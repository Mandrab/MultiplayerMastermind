package message

import akka.actor.typed.ActorRef

interface Message {
    val sender: ActorRef<Message>
}