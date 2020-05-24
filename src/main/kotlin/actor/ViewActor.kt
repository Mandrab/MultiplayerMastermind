package actor

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import akka.actor.typed.receptionist.Receptionist
import message.CheckResult
import message.Message
import message.StartGame
import view.View

class ViewActor(context: ActorContext<Message>, private val view: View) : AbstractBehavior<Message>(context) {
    override fun createReceive(): Receive<Message> = newReceiveBuilder().onMessage(StartGame::class.java) { msg ->
        context.system.receptionist().tell(Receptionist.find(ArbiterService.arbiterServiceKey,
                context.messageAdapter(Receptionist.Listing::class.java) { ListingResponse(context.self, msg, it) }))
        this@ViewActor
    }.onMessage(CheckResult::class.java) {
        view.newResult(it.mainReceiver.toString(), it.sender.toString(), it.correctPlace, it.wrongPlace)
        this@ViewActor
    }.build()

    companion object {
        fun create(view: View): Behavior<Message> = Behaviors.setup { ViewActor(it, view) }
    }

    private class ListingResponse (override val sender: ActorRef<Message>, msg: StartGame, listing: Receptionist.Listing) : Message {
        init {
            listing.allServiceInstances(ArbiterService.arbiterServiceKey).foreach {
                it.tell(StartGame(sender, msg.playerCount, msg.secretValueLength, emptyList()))
            }
        }
    }
}