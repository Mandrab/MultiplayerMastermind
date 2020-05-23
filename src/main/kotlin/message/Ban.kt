package message

import akka.actor.typed.ActorRef

data class Ban(override val sender: ActorRef<Message>, val playerID: String): Message