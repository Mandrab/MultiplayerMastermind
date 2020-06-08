package actor

import akka.actor.ActorRef
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Adapter
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import message.Message
import scala.collection.immutable.Set

typealias TypedActorRef<T> = akka.actor.typed.ActorRef<T>

/**
 * This class gives the possibility to register for the reception of some messages including StartGame and StopGame.
 * It provides the ability to send messages to every subscriber (broadcast) or to a single actor of them (unicast).
 *
 * @author Baldini Paolo, Battistini Ylenia.
 */
object Services {
    enum class Service(val key: ServiceKey<Message>) {
        START_GAME(ServiceKey.create(Message::class.java, "startGame")),
        STOP_GAME(ServiceKey.create(Message::class.java, "stopGame")),
        OBSERVE_RESULT(ServiceKey.create(Message::class.java, "attemptResult"))
    }

    fun unicast(key: ServiceKey<Message>, context: ActorContext<Message>, msg: Message? = null) = context.system
            .receptionist().tell(Receptionist.find(key, context.messageAdapter(Receptionist.Listing::class.java) {
                Unicast(msg, it, key) } ))

    fun broadcastList(key: ServiceKey<Message>, context: ActorContext<Message>, msg: Message? = null) = context.system
            .receptionist().tell(Receptionist.find(key, context.messageAdapter(Receptionist.Listing::class.java) {
                Broadcast(msg, it, key) }))

    class Unicast (val msg: Message?, private val listing: Receptionist.Listing, key: ServiceKey<Message>?): Message {
        override val sender: TypedActorRef<Message> = Adapter.toTyped(ActorRef.noSender())
        val actor = if (listing.allServiceInstances(key).isEmpty) null else listing.allServiceInstances(key).head()
    }

    class Broadcast (val msg: Message?, listing: Receptionist.Listing, key: ServiceKey<Message>?): Message {
        override val sender: TypedActorRef<Message> = Adapter.toTyped(ActorRef.noSender())
        val actors: Set<TypedActorRef<Message>> = listing.allServiceInstances(key)
    }
}