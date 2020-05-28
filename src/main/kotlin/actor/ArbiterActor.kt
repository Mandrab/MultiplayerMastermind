package actor

import akka.actor.AbstractActor
import akka.actor.ReceiveTimeout
import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.Adapter
import akka.actor.typed.receptionist.Receptionist
import message.*
import java.time.Duration
import kotlin.random.Random

class ArbiterActor: AbstractActor() {
    private val typedSelf = Adapter.toTyped<Message>(self)

    private val players: MutableMap<String, ActorRef<Message>> = mutableMapOf()
    private val playersCount: Int
        get() = players.size

    private var secretValueLength: Int = 0
    private var turnNumber: Int = 0
    private var humanPlayer: Boolean = false

    private var numberOfCheck = 0
    private var correctDigits = 0

    private lateinit var playerTurn: Iterator<String>
    private lateinit var turnPlayerID: String
    private lateinit var attempterPlayer: ActorRef<Message>

    private lateinit var lastGuess: Guess

    private lateinit var viewActor:ActorRef<Message>

    override fun preStart() = super.preStart().also {
        Adapter.toTyped(context.system).receptionist()
                .tell(Receptionist.register(Services.Service.START_GAME.key, Adapter.toTyped(self)))
        Adapter.toTyped(context.system).receptionist()
                .tell(Receptionist.register(Services.Service.STOP_GAME.key, Adapter.toTyped(self))) }

    override fun createReceive(): Receive = receiveBuilder()
            .match(StartGame::class.java, start)
            .match(StopGame::class.java, endGame)
            .build()

    private fun receiveGuess(): Receive = receiveBuilder()
            .match(Guess::class.java, guess)
            .match(Try::class.java) { context.become(receiveTryWin()); tryWin(it) }
            .match(ReceiveTimeout::class.java) { viewActor.tell(LostTurn(typedSelf,"The $turnPlayerID lost turn", this.turnPlayerID, this.turnNumber)); println("The $turnPlayerID lost turn"); turn() }
            .match(StopGame::class.java, endGame)
            .build()

    private fun receiveCheckGuess(): Receive = receiveBuilder()
            .match(CheckResult::class.java, guessResult)
            .match(ReceiveTimeout::class.java) { players[lastGuess.defenderID]
                    ?.tell(Check(typedSelf, lastGuess.attempt, lastGuess.attackerID, lastGuess.defenderID)) }
            .match(StopGame::class.java, endGame)
            .build()

    private fun receiveTryWin(): Receive = receiveBuilder()
            .match(Try::class.java, tryWin)
            .match(CheckResult::class.java, checkWin)
            .match(StopGame::class.java, endGame)
            .match(ReceiveTimeout::class.java) { viewActor.tell(LostTurn(typedSelf,"The $turnPlayerID lost turn", this.turnPlayerID, this.turnNumber)); println("The $turnPlayerID lost turn"); turn() }
            .build()


    private val start: (StartGame) -> Unit = { msg ->
        viewActor = msg.sender
        secretValueLength = msg.secretLength
        humanPlayer = msg.humanPlayer
        players.putAll((0 until msg.playerCount).map { Pair("Player$it", Adapter.spawn(context,
                PlayerActor.create("Player$it"), "Player$it")) })

        players.values.onEach { it.tell(StartGame(typedSelf, playersCount, secretValueLength, humanPlayer, players.values.toList())) }
        msg.sender.tell(GamePlayers(typedSelf, players.values.toList()))
        if(humanPlayer) playersCount+1
        playerTurn = players.keys.sortedBy { Random.nextInt(playersCount) }.iterator()

        context.receiveTimeout = Duration.ofSeconds(3)

        println("\n============Turn ${turnNumber++}============")
        turn()
    }

    private val guess: (Guess) -> Unit = {
        if (it.attackerID == turnPlayerID) {
            lastGuess = it
            context.become(receiveCheckGuess())
            players[it.defenderID]?.tell(Check(typedSelf, it.attempt, it.attackerID, it.defenderID))
            attempterPlayer = it.sender
        }
    }

    private val guessResult: (result: CheckResult) -> Unit = {
        println("$turnPlayerID got ${it.black} black and ${it.white} white.")
        context.become(receiveTryWin())
        attempterPlayer.tell(WannaTry(typedSelf, turnNumber))
    }

    private val tryWin: (Try) -> Unit = {
        if (it.attempt?.size == playersCount) it.attempt.forEachIndexed { idx, code ->
            val player = players.entries.elementAt(idx)
            player.value.tell(Check(typedSelf, code, self.path().name(), player.key))
        } else turn()
    }

    private fun turn() {
        if (!playerTurn.hasNext()) {
            println("\n============Turn ${turnNumber++}============")
            playerTurn = players.keys.sortedBy { Random.nextInt(playersCount) }.iterator()
        }
        turnPlayerID = playerTurn.next()

        context.become(receiveGuess())
        if(turnPlayerID.equals("Player0") && humanPlayer) {
            viewActor.tell(ExecTurn(typedSelf, turnNumber))
        }else  players[turnPlayerID]?.tell(ExecTurn(typedSelf, turnNumber))
    }

    private val checkWin: (response: CheckResult) -> Unit = {
        correctDigits += it.black
        if (++numberOfCheck == playersCount) { // rappresenta il numero di tentativi di vincita sottomessi
            if (correctDigits == playersCount * secretValueLength) {
                players.values.forEach { it.tell(End(typedSelf, turnPlayerID)) }
                viewActor.tell(End(typedSelf, turnPlayerID))
                println("Game ended. The winner is: $turnPlayerID")
                endGame(null)
            } else {
                numberOfCheck = 0
                correctDigits = 0

                players.values.forEach { it.tell(Ban(typedSelf, turnPlayerID)) }
                players.remove(turnPlayerID)
                viewActor.tell(Ban(typedSelf, turnPlayerID))
                println("$turnPlayerID has lost the game")

                if (playersCount == 0){
                    viewActor.tell(End(typedSelf , ""))
                    println("No one won the game")
                    endGame(null)
                }
                else turn()
            }
        }
    }

    private val endGame: (StopGame?) -> Unit = {
        players.values.forEach { it.tell(StopGame(typedSelf)) }
        context.children.forEach{ context.stop(it) } // così uccide i figli TODO serve sia la tell Stop che questa o ne basta una?
        context.stop(context.self) //così si auto uccide
    }
}