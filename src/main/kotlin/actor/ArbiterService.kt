package actor

import akka.actor.ActorRef
import akka.actor.typed.receptionist.ServiceKey
import message.Message

object ArbiterService {
    val arbiterServiceKey = ServiceKey.create(Message::class.java, "arbiterService")

    data class Arbiter(private val replyTo: ActorRef)
}