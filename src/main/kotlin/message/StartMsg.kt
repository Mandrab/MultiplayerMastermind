package message

import akka.actor.ActorRef

data class StartMsg(val secretValueLength: Int, val player: ActorRef)
