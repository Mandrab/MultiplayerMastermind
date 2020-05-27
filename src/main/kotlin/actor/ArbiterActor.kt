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

    private var numberOfCheck = 0
    private var correctDigits = 0

    private lateinit var playerTurn: Iterator<String>
    private lateinit var turnPlayerID: String
    private lateinit var attempterPlayer: ActorRef<Message>

    private lateinit var lastGuess: Guess

    override fun preStart() = super.preStart().also { Adapter.toTyped(context.system).receptionist().tell(Receptionist
                .register(Services.Service.START_GAME.key, Adapter.toTyped(self))) }

    override fun createReceive(): Receive = receiveBuilder().match(StartGame::class.java, start).build()

    private fun receiveGuess(): Receive = receiveBuilder()
            .match(Guess::class.java, guess)
            .match(Try::class.java) { context.become(receiveTryWin()); tryWin(it.attempt) }
            .match(ReceiveTimeout::class.java) { println("The $turnPlayerID lost turn"); turn() }
            .build()

    private fun receiveCheckGuess(): Receive = receiveBuilder()
            .match(CheckResult::class.java, guessResult)
            .match(ReceiveTimeout::class.java) { players[lastGuess.defenderID]?.tell(Check(typedSelf, lastGuess.attempt)) }
            .build()

    private fun receiveTryWin(): Receive = receiveBuilder()
            .match(Try::class.java) { tryWin(it.attempt) }
            .match(CheckResult::class.java, checkWin)
            .match(ReceiveTimeout::class.java) { println("The $turnPlayerID lost turn"); turn() }
            .build()

    private fun gameEnd(): Receive = receiveBuilder().match(End::class.java) { TODO() }.build()

    private val start: (StartGame) -> Unit = { msg ->
        secretValueLength = msg.secretLength
        players.putAll((0 until msg.playerCount).map { Pair("Player$it", Adapter.spawn(context,
                PlayerActor.create("Player$it"), "Player$it")) })

        players.values.onEach { it.tell(StartGame(typedSelf, playersCount, secretValueLength, players.values.toList())) }
        msg.sender.tell(GamePlayers(typedSelf, players.values.toList()))
        playerTurn = players.keys.sortedBy { Random.nextInt(playersCount) }.iterator()

        context.receiveTimeout = Duration.ofSeconds(3)

        println("\n============Turn ${turnNumber++}============")
        turn()
    }

    private val guess: (Guess) -> Unit = {
        if (it.attackerID == turnPlayerID) {
            lastGuess = it
            context.become(receiveCheckGuess())
            players[it.defenderID]?.tell(Check(typedSelf, it.attempt))
            attempterPlayer = it.sender
        }
    }

    private val guessResult: (result: CheckResult) -> Unit = {
        println("$turnPlayerID got ${it.black} black and ${it.white} white.")
        context.become(receiveTryWin())
        attempterPlayer.tell(WannaTry(typedSelf, turnNumber))
    }

    private fun tryWin(attempt: Array<Array<Int>>?) {
        attempt?.forEachIndexed { idx, code -> players.values.elementAt(idx).tell(Check(typedSelf, code))} ?: turn()
    }

    private fun turn() {
        if (!playerTurn.hasNext()) {
            println("\n============Turn ${turnNumber++}============")
            playerTurn = players.keys.sortedBy { Random.nextInt(playersCount) }.iterator()
        }
        turnPlayerID = playerTurn.next()

        context.become(receiveGuess())
        players[turnPlayerID]?.tell(ExecTurn(typedSelf, turnNumber))
    }

    private val checkWin: (response: CheckResult) -> Unit = {
        correctDigits += it.black
        if (++numberOfCheck == playersCount) { // rappresenta il numero di tentativi di vincita sottomessi
            if (correctDigits == playersCount * secretValueLength) {
                players.values.forEach { it.tell(End(typedSelf, turnPlayerID)) }
                println("Game ended. The winner is: $turnPlayerID")
                context.become(gameEnd())
            } else {
                numberOfCheck = 0
                correctDigits = 0

                players.values.forEach { it.tell(Ban(typedSelf, turnPlayerID)) }
                players.remove(turnPlayerID)
                println("$turnPlayerID has lost the game")

                if (playersCount == 0) println("No one won the game")
                else turn()
            }
        }
    }
}