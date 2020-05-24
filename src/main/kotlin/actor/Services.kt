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
    enum class Service(val key: ServiceKey<Message>) {
        START_GAME(ServiceKey.create(Message::class.java, "startGame")),
        OBSERVE_RESULT(ServiceKey.create(Message::class.java, "attemptResult"))
    }

    fun unicast(key: ServiceKey<Message>, context: ActorContext<Message>, msg: Message? = null) = context.system
            .receptionist().tell(Receptionist.find(key, context.messageAdapter(Receptionist.Listing::class.java) {
                Unicast(msg, it, key) } ))

    fun broadcastList(key: ServiceKey<Message>, context: ActorContext<Message>, msg: Message? = null) = context.system
            .receptionist().tell(Receptionist.find(key, context.messageAdapter(Receptionist.Listing::class.java) {
                Broadcast(msg, it, key) }))

    class Unicast (val msg: Message?, listing: Receptionist.Listing, key: ServiceKey<Message>?): Message {
        override val sender: TypedActorRef<Message> = Adapter.toTyped(ActorRef.noSender())
        val actor: TypedActorRef<Message> = listing.allServiceInstances(key).last()
    }

    class Broadcast (val msg: Message?, listing: Receptionist.Listing, key: ServiceKey<Message>?): Message {
        override val sender: TypedActorRef<Message> = Adapter.toTyped(ActorRef.noSender())
        val actors: Set<TypedActorRef<Message>> = listing.allServiceInstances(key)
    }
}