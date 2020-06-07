package actor

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.receptionist.Receptionist
import algorithm.Code
import message.*
import view.HumanView
/*
This class represent a HumanPlayerActor.
This class implements AbstractPlayerActor class because the human player is considered like the others.
It can receive the same messages but when it receives them it manages them using a HumanView.

@author Baldini Paolo, Battistini Ylenia
 */
class HumanPlayerActor private constructor(
        context: ActorContext<Message>,
        private val playerID: String,
        override var secret: Code,
        private val view: HumanView,
        private val arbiter: ActorRef<Message>
) : AbstractPlayerActor(context) {

    override val onAny: (Message) -> Behavior<Message> = { apply {
        if (it is Guess) arbiter.tell(it)
        if (it is Try) arbiter.tell(it)
    } }

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

    /*
    The human player registers to be able to receive attempts.
     */
    companion object {
        fun create(ID: String, secret: Array<Int>, view: HumanView, arbiter: ActorRef<Message>): Behavior<Message> =
                Behaviors.setup {
            it.system.receptionist().tell(Receptionist.register(Services.Service.OBSERVE_RESULT.key, it.self))
            HumanPlayerActor(it, ID, Code(secret), view, arbiter)
        }
    }
}