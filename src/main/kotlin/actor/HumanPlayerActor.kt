package actor

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.receptionist.Receptionist
import algorithm.Code
import message.*
import view.HumanView

class HumanPlayerActor private constructor(
        context: ActorContext<Message>,
        private val playerID: String,
        private val view: HumanView
) : AbstractPlayerActor(context) {
    override lateinit var secret: Code

    override val checkResult: (CheckResult) -> Behavior<Message> = { apply {
        // TODO
    } }

    override val execTurn: (ExecTurn) -> Behavior<Message> = { apply {
        // TODO
    } }

    override val wannaTry: (WannaTry) -> Behavior<Message> = { apply {
        // TODO
    } }

    override val ban: (Ban) -> Behavior<Message> = { apply {
        // TODO
    } }

    override val stopGame: (StopGame) -> Behavior<Message> = { apply {
        // TODO
    } }

    override val end: (End) -> Behavior<Message> = { apply {
        // TODO
    } }

    companion object {
        fun create(ID: String, view: HumanView): Behavior<Message> = Behaviors.setup {
            it.system.receptionist().tell(Receptionist.register(Services.Service.OBSERVE_RESULT.key, it.self))
            HumanPlayerActor(it, ID, view)
        }
    }
}