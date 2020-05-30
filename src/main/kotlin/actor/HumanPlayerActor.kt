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
        view.newResult(it.defenderID, it.black, it.white)
    } }

    override val execTurn: (ExecTurn) -> Behavior<Message> = { apply {
        view.execTurn(it.turn)
    } }

    override val wannaTry: (WannaTry) -> Behavior<Message> = { apply {
        view.wannaTry()
    } }

    override val lostTurn: (LostTurn) -> Behavior<Message> = { apply {
        view.lostTurn(it.turn)
    } }

    override val ban: (Ban) -> Behavior<Message> = {
        view.ban()
        Behaviors.stopped()
    }

    override val stopGame: (StopGame) -> Behavior<Message> = {
        view.stop()
        Behaviors.stopped()
    }

    override val end: (End) -> Behavior<Message> = {
        view.endGame(it.winnerID == playerID)
        Behaviors.stopped()
    }

    companion object {
        fun create(ID: String, view: HumanView): Behavior<Message> = Behaviors.setup {
            it.system.receptionist().tell(Receptionist.register(Services.Service.OBSERVE_RESULT.key, it.self))
            HumanPlayerActor(it, ID, view)
        }
    }
}