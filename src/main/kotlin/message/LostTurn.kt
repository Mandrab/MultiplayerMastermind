package message

import akka.actor.typed.ActorRef

data class LostTurn(override val sender: ActorRef<Message>, val lostTurn : String) : Message