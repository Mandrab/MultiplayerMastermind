package message

import akka.actor.typed.ActorRef

data class End(override val sender: ActorRef<Message>, val winnerID: String): Message
