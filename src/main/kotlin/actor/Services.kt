package actor

import akka.actor.ActorRef
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Adapter
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import message.Message
import scala.collection.immutable.Set

typealias TypedActorRef<T> = akka.actor.typed.ActorRef<T>

object Services {

    val startGameServiceKey: ServiceKey<Message> = ServiceKey.create(Message::class.java, "startGame")

    val observeResultService = ServiceKey.create(Message::class.java, "playerService")

    fun broadcastList(key: ServiceKey<Message>, context: ActorContext<Message>, msg: Message? = null) = context.system
            .receptionist().tell(Receptionist.find(key, listingAdapter(context, key, msg)))

    private fun listingAdapter(context: ActorContext<Message>, key: ServiceKey<Message>? = null, msg: Message? = null)
            : TypedActorRef<Receptionist.Listing> = context.messageAdapter(Receptionist.Listing::class.java) {
        Broadcast(msg, it, key) }

    class Broadcast (val msg: Message?, listing: Receptionist.Listing, key: ServiceKey<Message>?): Message {
        override val sender: TypedActorRef<Message> = Adapter.toTyped(ActorRef.noSender())
        val actors: Set<TypedActorRef<Message>> = listing.allServiceInstances(key)
    }
}