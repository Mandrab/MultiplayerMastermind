package agent

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import algorithm.AttackerStrategy
import algorithm.Code
import algorithm.CodeMaker
import algorithm.Result
import message.*

class PlayerAgent private constructor(
        context: ActorContext<Message>,
        private val ID: String,
        private val playerNum: Int,
        private val valueSize: Int
): AbstractBehavior<Message>(context), CodeMaker {
    private val gameState = Array(playerNum -1) { AttackerStrategy() }
    private var lastAttemptPlayer: AttackerStrategy? = null
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

    fun idle(): Behavior<Message> = newReceiveBuilder().onMessage(StartGame::class.java) { this }.build()

    private val execTurn: (ExecTurn) -> Behavior<Message> = { exec -> also {
        lastAttemptPlayer = gameState.firstOrNull { it.ready }?.also {
            val attempt = Guess(context.self, exec.turn, it.makeAttempt().code.toTypedArray(), ID)
            exec.sender.tell(attempt)
        }
    } }

    private val check: (Check) -> Behavior<Message> = { check -> also {
        val result = secret.guess(Code(check.attempt))
        check.sender.tell(CheckResult(context.self, result.black, result.white))
    } }

    private val checkResult: (CheckResult) -> Behavior<Message> = { result -> also {
        if (waitingCheck) lastAttemptPlayer?.let {
            // TODO update result
            it.attemptResult(Result(result.correctPlace, result.wrongPlace))
            it.tickSearch(30)
            context.self.tell(Update())
        }
    } }

    private val wannaTry: (WannaTry) -> Behavior<Message> = { result -> also {
        if (gameState.all { it.found }) {
            result.sender.tell(Try(context.self, result.turn, gameState.map { it.makeAttempt().code.toTypedArray() }
                    .toTypedArray()))
        } else result.sender.tell(Try(context.self, result.turn, null))
    } }

    private val update: (Update) -> Behavior<Message> = { _ -> also {
        gameState.firstOrNull { !it.ready }?.tickSearch(30)
    } }

    companion object {
        fun create(ID: String, players: Int, secret: Int): Behavior<Message> = Behaviors.setup {
            PlayerAgent(it, ID, players, secret).idle()
        }
    }

    private inner class Update: Message { override val sender: ActorRef<Message> = context.self }
}