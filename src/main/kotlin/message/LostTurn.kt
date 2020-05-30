package message

import akka.actor.typed.ActorRef

data class LostTurn(override val sender: ActorRef<Message>, val attackerID: String, val turn:Int) : Message