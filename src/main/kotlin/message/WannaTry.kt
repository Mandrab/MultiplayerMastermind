package message

import akka.actor.typed.ActorRef

data class WannaTry(override val sender: ActorRef<Message>, val turn: Int): Message
