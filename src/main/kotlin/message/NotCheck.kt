package message

import akka.actor.typed.ActorRef

data class NotCheck(override val sender: ActorRef<Message>, val idPlayer: String, val turn:Int) : Message