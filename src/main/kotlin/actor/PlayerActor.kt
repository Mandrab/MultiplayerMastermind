package actor

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.*
import algorithm.AttackerStrategy
import algorithm.Code
import algorithm.CodeMaker
import algorithm.Result
import message.*


class PlayerActor private constructor(
        context: ActorContext<Message>,
        private val playerID: String
) : AbstractBehavior<Message>(context), CodeMaker {
    private var valueSize: Int = 4
        set(value) {
            field = value
            Code.secretLength = value
        }
    private var playerNum: Int = 1
        set(value) {
            field = value
            Array(playerNum -1) { AttackerStrategy() }
        }

    private lateinit var gameState: Map<ActorRef<Message>, AttackerStrategy>
    private var lastAttemptPlayer: Pair<ActorRef<Message>, AttackerStrategy>? = null
    private var waitingCheck = false

    override val secret = Code()
    override fun verify(guess: Code) = secret.guess(guess)

    override fun createReceive(): Receive<Message> = newReceiveBuilder()
            .onMessage(Ban::class.java) { Behaviors.stopped() }
            .onMessage(Stop::class.java) { Behaviors.stopped() }
            .onMessage(End::class.java) { Behaviors.stopped() }
            .onMessage(ExecTurn::class.java, execTurn)
            .onMessage(Check::class.java, check)
            .onMessage(CheckResult::class.java, checkResult)
            .onMessage(WannaTry::class.java, wannaTry)
            .onMessage(Update::class.java, update)
            .build()

    fun idle(): Behavior<Message> = Behaviors.receive(Message::class.java).onMessage(StartGame::class.java) { it ->
        gameState = it.players.map { Pair(it, AttackerStrategy()) }.toMap()
        this
    }.build()

    private val execTurn: (ExecTurn) -> Behavior<Message> = { exec -> also {
        lastAttemptPlayer = gameState.entries.firstOrNull { it.value.ready }?.also {
            val attempt = Guess(context.self, exec.turn, it.value.makeAttempt().code.toTypedArray(), playerID)
            exec.sender.tell(attempt)
        }?.toPair()
    } }

    private val check: (Check) -> Behavior<Message> = { check -> also {
        val result = secret.guess(Code(check.attempt))
        // TODO send to all
        val checkResult = CheckResult(context.self, result.black, result.white, playerID)
        check.sender.tell(checkResult)
        gameState.entries.forEach { it.key.tell(checkResult) }
    } }

    private val checkResult: (CheckResult) -> Behavior<Message> = { result -> also {
        if (waitingCheck) lastAttemptPlayer?.let {
            // TODO update result
            it.second.attemptResult(Result(result.correctPlace, result.wrongPlace))
            it.second.tickSearch(30)
            context.self.tell(Update())
        }
    } }

    private val wannaTry: (WannaTry) -> Behavior<Message> = { result -> also {
        if (gameState.all { it.value.found }) {
            result.sender.tell(Try(context.self, result.turn, gameState.map { it.value.makeAttempt().code.toTypedArray() }
                    .toTypedArray()))
        } else result.sender.tell(Try(context.self, result.turn, null))
    } }

    private val update: (Update) -> Behavior<Message> = { _ -> also {
        gameState.entries.firstOrNull { !it.value.ready }?.value?.tickSearch(30)
    } }

    companion object {
        fun create(ID: String): Behavior<Message> = Behaviors.setup { PlayerActor(it, ID).idle() }
    }

    private inner class Update: Message { override val sender: ActorRef<Message> = context.self }
}