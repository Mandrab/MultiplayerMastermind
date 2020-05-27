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

    private var tryWin = false

    private var numberOfCheck = 0
    private var correctDigits = 0

    private lateinit var playerTurn: Iterator<String>
    private lateinit var turnPlayerID: String
    private lateinit var attempterPlayer: ActorRef<Message>

    private var waitingWin = false

    private lateinit var lastGuess: Guess

    override fun preStart() = super.preStart().also { Adapter.toTyped(context.system).receptionist().tell(Receptionist
                .register(Services.Service.START_GAME.key, Adapter.toTyped(self))) }

    override fun createReceive(): Receive = receiveBuilder().match(StartGame::class.java, start).build()

    private fun receiveGuess(): Receive = receiveBuilder()
            .match(Guess::class.java) { msg -> guess(msg) }
            .match(ReceiveTimeout::class.java) {
                println("The $turnPlayerID lost turn")
                turn()
            }.build()

    private fun receiveCheckResult(): Receive = receiveBuilder()
            .match(CheckResult::class.java) { msg -> checkResult(attempterPlayer, msg.black, msg.white) }
            .match(ReceiveTimeout::class.java) {
                context.actorSelection(lastGuess.defenderID).tell(Check(typedSelf, lastGuess.attempt), self)
            }.build()

    private fun receiveTry(): Receive = receiveBuilder()
            .match(Try::class.java) { msg -> tryWin(msg.attempt) }
            .match(ReceiveTimeout::class.java) {
                println("The $turnPlayerID lost turn")
                turn()
            }.build()

    private val start: (StartGame) -> Unit = { msg ->
        secretValueLength = msg.secretLength
        players.putAll((0 until msg.playerCount).map { Pair("Player$it", Adapter.spawn(context,
                PlayerActor.create("Player$it"), "Player$it")) })
        //TODO ? Adapter.watch(context, player)
        players.values.onEach { it.tell(StartGame(typedSelf, playersCount, secretValueLength, players.values.toList())) }
        msg.sender.tell(GamePlayers(typedSelf, players.values.toList()))
        playerTurn = players.keys.sortedBy { Random.nextInt(playersCount) }.iterator()

        context.receiveTimeout = Duration.ofSeconds(5)
        turn()
    }

    /*private val timeout: (CheckTimeout) -> Unit = {
        //Thread.sleep(5) //TODO remove
        if (it.idPlayer == turnPlayerID && it.turn == turnNumber && !waitingWin) {
            if (System.currentTimeMillis() - it.time > 5000) {
                println("The $turnPlayerID lost turn")
                context.actorSelection(turnPlayerID).tell(LostTurn(typedSelf, "Lost Turn"), self)
            } else self.tell(it, self)
        }
    }*/

    private fun guess(msg: Guess) {
        println("guess")
        if (msg.attackerID == turnPlayerID) {
            lastGuess = msg
            context.become(receiveCheckResult())
            context.actorSelection(msg.defenderID).tell(Check(typedSelf, msg.attempt), self)
            attempterPlayer = msg.sender
        }
    }

    private fun checkResult(attempterPlayer: ActorRef<Message>, black: Int, white: Int) {
        if (!tryWin) {
            waitingWin = true
            println("$turnPlayerID got $black black and $white white.")
            context.become(receiveTry())
            attempterPlayer.tell(WannaTry(typedSelf, turnNumber))
        } else checkWin(black)
    }

    private fun tryWin(attempt: Array<Array<Int>>?) {
        attempt?.run {
            val children = context.children
            tryWin = true
            context.become(receiveCheckResult())
            children.forEachIndexed { i, c -> c.tell(Check(typedSelf, attempt[i]), self) }
        } ?: turn()
    }

    private fun turn() {
        if (!playerTurn.hasNext()) {
            println("turn ${turnNumber++}")
            playerTurn = players.keys.sortedBy { Random.nextInt(playersCount) }.iterator()
        }
        tryWin = false
        waitingWin = false
        turnPlayerID = playerTurn.next()

        context.become(receiveGuess())
        players[turnPlayerID]?.tell(ExecTurn(typedSelf, turnNumber))
        //context.actorSelection(turnPlayerID).tell(ExecTurn(typedSelf, turnNumber), self)
        //self.tell(CheckTimeout(turnPlayerID, turnNumber, System.currentTimeMillis()), self)
        /*
        val playerTurn = context.actorSelection(this.idPlayer)
        ...
        val timeout = Timeout.create(java.time.Duration.ofSeconds(5))
        val future = Patterns.ask(playerTurn, ExecTurn(typedSelf, turnNumber), timeout)

        try { // TODO
            Timer.schedule(
                delay: Long,
                crossinline action: TimerTask.() -> Unit
            ): TimerTask (source)
            Await.result(future, Duration.fromNanos(5*10.0.pow(9))).toString()
        }catch ( e:Exception ){
            lostTurn(this.idPlayer)
        }
        context.system.scheduler.scheduleOnce(Duration.ofMillis(10)!!, {
            TODO()
        }, context.system.dispatcher)*/
    }

    private fun checkWin(black: Int) {
        correctDigits += black
        if (++numberOfCheck == playersCount) { // rappresenta il numero di tentativi di vincita sottomessi
            if (correctDigits == playersCount * secretValueLength) {
                players.values.forEach { it.tell(End(typedSelf, turnPlayerID)) }
                println("Game ended. The winner is: $turnPlayerID")
            } else {
                numberOfCheck = 0
                correctDigits = 0

                players.values.forEach { it.tell(Ban(typedSelf, turnPlayerID)) }
                players.remove(turnPlayerID)
                println("$turnPlayerID has lost the game")
                turn()
            }
        }
    }

    private data class CheckTimeout(val idPlayer: String, val turn: Int, val time: Long)
}