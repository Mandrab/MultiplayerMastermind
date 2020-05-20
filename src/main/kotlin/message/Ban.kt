package message

import akka.actor.typed.ActorRef

data class Ban(val actor: ActorRef<Message>, val playerID: String): Message(actor)