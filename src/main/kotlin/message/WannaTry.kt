package message

import akka.actor.typed.ActorRef

data class WannaTry(val actor: ActorRef<Message>, val turn: Int): Message(actor)