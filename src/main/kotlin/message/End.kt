package message

import akka.actor.typed.ActorRef

data class End(val actor: ActorRef<Message>, val winnerID: String): Message(actor)