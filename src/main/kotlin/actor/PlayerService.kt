package actor

import akka.actor.typed.receptionist.ServiceKey
import message.Message

object PlayerService {
    val playerServiceKey: ServiceKey<Message> = ServiceKey.create(Message::class.java, "playerService")

    class Player {

    }
}