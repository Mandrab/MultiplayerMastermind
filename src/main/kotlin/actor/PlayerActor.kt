package actor

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.*
import akka.actor.typed.receptionist.Receptionist
import algorithm.AttackerStrategy
import algorithm.Code
import algorithm.CodeMaker
import algorithm.Result
import message.*

class PlayerActor private constructor(
        context: ActorContext<Message>,
        private val playerID: String,
        private val idx: Int
) : AbstractBehavior<Message>(context), CodeMaker {
    private lateinit var gameState: List<AttackerStrategy>
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
            .onMessage(Services.Broadcast::class.java, broadcast)
            .onMessage(WannaTry::class.java, wannaTry)
            .onMessage(Update::class.java, update)
            .build()

    fun idle(): Behavior<Message> = Behaviors.receive(Message::class.java).onMessage(StartGame::class.java) { apply {
        Code.secretLength = it.secretLength
        gameState = (0 until it.playerCount).map { AttackerStrategy() }
    } }.build()

    private val execTurn: (ExecTurn) -> Behavior<Message> = { exec -> also {
        println("execturn")
        println(gameState.filterIndexed { i, _ -> idx != i }.any { it.ready })
        lastAttemptPlayer = gameState.filterIndexed { i, _ -> idx != i }.firstOrNull { it.ready }?.also {
            val attempt = Guess(context.self, exec.turn, it.makeAttempt().code.toTypedArray(), playerID, "Player${gameState.indexOf(it)}")
            waitingCheck = true
            exec.sender.tell(attempt)
            println("send attempt")
        }
    } }

    private val check: (Check) -> Behavior<Message> = { check -> also {
        println("check")
        val result = secret.guess(Code(check.attempt))
        val checkResult = CheckResult(context.self, result.black, result.white, check.sender.path().name())// TODO check mainreceiver

        Services.broadcastList(Services.Service.OBSERVE_RESULT.key, context, checkResult)
        check.sender.tell(checkResult)
    } }

    private val checkResult: (CheckResult) -> Behavior<Message> = { result -> also {
        if (waitingCheck) lastAttemptPlayer?.let {
            // TODO update result
            it.attemptResult(Result(result.black, result.white))
            context.self.tell(Update())
        }
    } }

    private val broadcast: (Services.Broadcast) -> Behavior<Message> = { broadcast -> also {
        broadcast.actors.foreach { it.tell(broadcast.msg) }
    } }

    private val wannaTry: (WannaTry) -> Behavior<Message> = { result -> also {
        if (gameState.filterIndexed { i, _ -> idx != i }.all { it.found }) {
            result.sender.tell(Try(context.self, result.turn, gameState.map { it.makeAttempt().code.toTypedArray() }
                    .mapIndexed { i, e -> if (i == idx) secret.code.toTypedArray() else e }.toTypedArray()))
        } else result.sender.tell(Try(context.self, result.turn, null))
    } }

    private val update: (Update) -> Behavior<Message> = { also {
        gameState.firstOrNull { !it.ready }?.let {
            it.tickSearch(30)
            context.self.tell(Update())
        }
    } }

    companion object {
        fun create(ID: String, idx: Int): Behavior<Message> = Behaviors.setup {
            it.system.receptionist().tell(Receptionist.register(Services.Service.OBSERVE_RESULT.key, it.self))
            PlayerActor(it, ID, idx).idle()
        }
    }

    private inner class Update: Message { override val sender: ActorRef<Message> = context.self }
}