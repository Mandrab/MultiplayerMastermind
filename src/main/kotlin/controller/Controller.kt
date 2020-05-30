package controller

import actor.ArbiterActor
import actor.HumanPlayerActor
import actor.ViewActor
import akka.actor.typed.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.typed.javadsl.Adapter
import message.Message
import view.ViewImpl

typealias TypedActorSystem = akka.actor.typed.ActorSystem<Message>
typealias TypedProps = akka.actor.typed.Props

class Controller {
    private val system = ActorSystem.create("Mastermind")
    private val typedSystem = TypedActorSystem.wrap(system)
    private val arbiter = Adapter.toTyped<Message>(system.actorOf(Props.create(ArbiterActor::class.java), "Arbiter"))

    private val view = ViewImpl(this)

    fun humanPlayer(secret: Array<Int>): ActorRef<Message> = typedSystem.systemActorOf(HumanPlayerActor
            .create("Player0", secret, view.humanView, arbiter), "Player0", TypedProps.empty())

    init {
        view.gameView.actor = typedSystem.systemActorOf(ViewActor.create(view.gameView), "ViewActor", TypedProps.empty())
    }
}