package controller

import actor.ArbiterActor
import actor.HumanPlayerActor
import actor.ViewActor
import akka.actor.typed.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import message.Message
import view.ViewImpl

typealias TypedActorSystem = akka.actor.typed.ActorSystem<Message>
typealias TypedProps = akka.actor.typed.Props

class Controller {
    private val system = ActorSystem.create("Mastermind")
    private val typedSystem = TypedActorSystem.wrap(system)

    private val view = ViewImpl(this)

    val humanPlayer: ActorRef<Message> by lazy {
        typedSystem.systemActorOf(HumanPlayerActor.create("Player0", view.humanView), "Player0", TypedProps.empty())
    }

    init {
        system.actorOf(Props.create(ArbiterActor::class.java), "Arbiter")

        view.gameView.actor = typedSystem.systemActorOf(ViewActor.create(view.gameView), "ViewActor", TypedProps.empty())
    }
}