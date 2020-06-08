package actor

import akka.actor.AbstractActor
import akka.actor.ReceiveTimeout
import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.Adapter
import akka.actor.typed.receptionist.Receptionist
import message.*
import java.time.Duration
import kotlin.random.Random

/**
 * This is a ArbiterActor class.
 * This class contains the management of the game by sending messages to the players.
 *
 * @author Baldini Paolo, Battistini Ylenia
 */
class ArbiterActor: AbstractActor() {
    private val typedSelf = Adapter.toTyped<Message>(self)

    private val players: MutableMap<String, ActorRef<Message>> = mutableMapOf()
    private val playersCount: Int
        get() = players.size

    private var secretValueLength: Int = 0
    private var turnNumber: Int = 0

    private lateinit var playerTurn: Iterator<String>
    private lateinit var turnPlayerID: String
    private lateinit var attempterPlayer: ActorRef<Message>

    private lateinit var lastGuess: Guess
    private var lastTryResults: MutableMap<String, Boolean>? = null
    private var lastTry: Try? = null

    private lateinit var viewActor:ActorRef<Message>

    /**
     * First arbiterActor subscribes themselves to the reception of the start and stop messages.
     */
    override fun preStart() {
        super.preStart()
        Adapter.toTyped(context.system).receptionist().tell(Receptionist.register(Services.Service.START_GAME.key,
                Adapter.toTyped(self)))
        Adapter.toTyped(context.system).receptionist().tell(Receptionist.register(Services.Service.STOP_GAME.key,
                Adapter.toTyped(self)))
    }

    /**
     * When arbiter received startGame he call start method and when it received stopGame he call endGame method.
     */
    override fun createReceive(): Receive = receiveBuilder()
            .match(StartGame::class.java, start)
            .match(StopGame::class.java, endGame)
            .build()

    /**
     * When arbiter received Guess message he call guess method and when it received stopGame he call endGame method.
     * If arbiter received Try message, check if the player who sent the message is the one who has the turn.
     * If so then change its behavior to receive tryWin.
     * If arbiter received ReceiveTimeout message sends LostTurn message to player and view and he call turn method.
     */
    private fun receiveGuess(): Receive = receiveBuilder()
            .match(Guess::class.java, guess)
            .match(Try::class.java) { if (it.sender == attempterPlayer) context.become(receiveTryWin()); tryWin(it) }
            .match(ReceiveTimeout::class.java) {
                attempterPlayer.tell(LostTurn(typedSelf, turnPlayerID, turnNumber))
                viewActor.tell(LostTurn(typedSelf, turnPlayerID, turnNumber))
                println("The $turnPlayerID lost turn")
                turn()
            }
            .match(StopGame::class.java, endGame)
            .build()

    /**
     * When arbiter received CheckResult message he call guessResult method and when it received stopGame he call endGame method.
     * If arbiter received ReceiveTimeout message check that the player is not null and postpone the Check message.
     */
    private fun receiveCheckGuess(): Receive = receiveBuilder()
            .match(CheckResult::class.java, guessResult)
            .match(ReceiveTimeout::class.java) { players[lastGuess.defenderID]?.tell(Check(typedSelf, lastGuess.attempt,
                    lastGuess.attackerID, lastGuess.defenderID, lastGuess.turn)) }
            .match(StopGame::class.java, endGame)
            .build()


    /**
     * When arbiter received Try message he call tryWin method, when received CheckResult message he call a checkWin method
     * and when it received stopGame he call endGame method.
     * If arbiter received ReceiveTimeout message look for the player for which there is last attempt and postpone Check message.
     * If last attempt is null send LostTurn message to viewActor.
     */
    private fun receiveTryWin(): Receive = receiveBuilder()
            .match(Try::class.java, tryWin)
            .match(CheckResult::class.java, checkWin)
            .match(ReceiveTimeout::class.java) {
                lastTry?.let { t ->
                    players.keys.filterNot { lastTryResults?.keys?.contains(it) ?: false }.forEachIndexed { idx, it ->
                        players[it]!!.tell(Check(typedSelf, t.attempt!![idx], t.sender.path().name(), it, turnNumber))
                } } ?: let {
                    viewActor.tell(LostTurn(typedSelf, turnPlayerID, turnNumber))
                    println("The $turnPlayerID lost turn")
                    turn()
                }
            }
            .match(StopGame::class.java, endGame)
            .build()

    /**
     * This is a start method.
     * When the arbiter received a StartGame message he creates the player and sends the StartGame message.
     * If there is also a human palyer he creates a HumanPlayerActor.
     */
    private val start: (StartGame) -> Unit = { msg ->
        viewActor = msg.sender
        secretValueLength = msg.secretLength

        msg.humanPlayer?.let { players["Player0"] = it }
        while (playersCount < msg.playerCount)
            "Player${players.size}".let { players[it] = Adapter.spawn(context, PlayerActor.create(it), it) }

        msg.sender.tell(GamePlayers(typedSelf, players.values.toList()))
        players.values.onEach {
            it.tell(StartGame(typedSelf, playersCount, secretValueLength, null, players.values.toList()))
        }
        playerTurn = players.keys.sortedBy { Random.nextInt(playersCount) }.iterator()

        context.receiveTimeout = Duration.ofSeconds(3)

        println("\n============Turn ${turnNumber++}============")
        turn()
    }

    /**
     * This is a guess method.
     * When the arbiter received a Guess message check if the player who sent the message is the one who has the turn and change
     * its behaviour.
     * Also send to defender a Check message.
     */
    private val guess: (Guess) -> Unit = {
        if (it.attackerID == turnPlayerID) {
            lastGuess = it
            context.become(receiveCheckGuess())
            players[it.defenderID]?.tell(Check(typedSelf, it.attempt, it.attackerID, it.defenderID, it.turn))
        }
    }

    /**
     * This is a guessResult method.
     * When arbiter received a CheckResult message checks and change it behaviour in receiveTryWin.
     * Also send a WannaTry message to player who sent the message.
     */
    private val guessResult: (result: CheckResult) -> Unit = { r ->
        if (lastGuess.attackerID == r.attackerID && lastGuess.defenderID == r.defenderID && lastGuess.turn == r.turn) {
            println("$turnPlayerID got ${r.black} black and ${r.white} white.")
            context.become(receiveTryWin())
            attempterPlayer.tell(WannaTry(typedSelf, turnNumber))
        }
    }

    /**
     * This is a tryWin method.
     * When arbiter received a Try message check if the player who sent the message is the one who has the turn and if the turn matches
     * and foreach player send a Check message.
     */
    private val tryWin: (Try) -> Unit = { t ->
        if (t.sender == attempterPlayer && t.turn == turnNumber) {
            t.attempt?.apply {
                lastTryResults = mutableMapOf()
                lastTry = t
            }
            if (t.attempt?.size == playersCount) t.attempt.forEachIndexed { idx, code ->
                val player = players.entries.elementAt(idx)
                player.value.tell(Check(typedSelf, code, t.sender.path().name(), player.key, turnNumber))
            } else turn()
        }
    }

    /**
     * This method manages turn randomly.
     * For each turn, send a ExecTurn message to the player that it's his turn.
     */
    private fun turn() {
        if (!playerTurn.hasNext()) {
            println("\n============Turn ${turnNumber++}============")
            playerTurn = players.keys.sortedBy { Random.nextInt(playersCount) }.iterator()
        }
        turnPlayerID = playerTurn.next()
        attempterPlayer = players[turnPlayerID]!!
        context.become(receiveGuess())
        players[turnPlayerID]?.tell(ExecTurn(typedSelf, turnNumber))
    }

    /**
     * This is a checkWin method.
     * When arbiter received a CheckResult message he
     * check if a black value are equal to the length of the secret value and,
     * if the player has guessed the number for each player, send End message to everyone,
     * else send a Ban message to player who sent CheckResult.
     */
    private val checkWin: (response: CheckResult) -> Unit = {
        if (lastTry != null && it.attackerID == turnPlayerID && it.turn == turnNumber) {
            lastTryResults!![it.defenderID] = secretValueLength == it.black

            if (secretValueLength == it.black) {
                if (lastTryResults!!.size == playersCount) {
                    players.values.forEach { it.tell(End(typedSelf, turnPlayerID)) }
                    viewActor.tell(End(typedSelf, turnPlayerID))
                    println("Game ended. The winner is: $turnPlayerID")
                    endGame(null)
                }
            } else {
                lastTryResults = null
                lastTry = null

                players.values.forEach { it.tell(Ban(typedSelf, turnPlayerID)) }
                viewActor.tell(Ban(typedSelf, turnPlayerID))
                players.remove(turnPlayerID)
                println("$turnPlayerID has lost the game")

                if (playersCount == 0) {
                    viewActor.tell(End(typedSelf, ""))
                    println("No one won the game")
                    endGame(null)
                } else turn()
            }
        }
    }

    /**
     * This is a endGame method.
     * When arbiter received a StopGame message send it message to all the children, then kill it and kill yourself.
     */
    private val endGame: (StopGame?) -> Unit = {
        players.values.forEach { it.tell(StopGame(typedSelf)) }
        context.children.forEach { context.stop(it) } // così uccide i figli
        context.stop(context.self) //così si auto uccide
    }
}