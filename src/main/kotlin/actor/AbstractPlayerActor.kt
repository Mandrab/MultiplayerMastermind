package actor

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.*
import algorithm.Code
import algorithm.CodeMaker
import message.*

abstract class AbstractPlayerActor(context: ActorContext<Message>) : AbstractBehavior<Message>(context), CodeMaker {

    abstract override val secret: Code

    override fun verify(guess: Code) = secret.guess(guess)

    override fun createReceive(): Receive<Message> = newReceiveBuilder()
            .onMessage(ExecTurn::class.java, execTurn)
            .onMessage(Check::class.java, check)
            .onMessage(CheckResult::class.java, checkResult)
            .onMessage(Services.Broadcast::class.java, broadcast)
            .onMessage(WannaTry::class.java, wannaTry)
            .onMessage(LostTurn::class.java, lostTurn)
            .onMessage(Ban::class.java, ban)
            .onMessage(StopGame::class.java, stopGame)
            .onMessage(End::class.java, end)
            .onAnyMessage(onAny)
            .build()

    private val check: (Check) -> Behavior<Message> = { check -> also {
        val result = secret.guess(Code(check.attempt))
        val checkResult = CheckResult(context.self, result.black, result.white, check.attackerID, check.defenderID) // TODO check mainreceiver

        Services.broadcastList(Services.Service.OBSERVE_RESULT.key, context, checkResult)
        check.sender.tell(checkResult)
    } }

    private val broadcast: (Services.Broadcast) -> Behavior<Message> = { apply {
        it.actors.foreach { act -> act.tell(it.msg) }
    } }

    open val onAny: (Message) -> Behavior<Message> = { this }

    abstract val execTurn: (ExecTurn) -> Behavior<Message>

    abstract val checkResult: (CheckResult) -> Behavior<Message>

    abstract val wannaTry: (WannaTry) -> Behavior<Message>

    abstract val lostTurn: (LostTurn) -> Behavior<Message>

    abstract val ban: (Ban) -> Behavior<Message>

    abstract val stopGame: (StopGame) -> Behavior<Message>

    abstract val end: (End) -> Behavior<Message>
}