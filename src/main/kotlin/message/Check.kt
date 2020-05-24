package message

import akka.actor.typed.ActorRef

data class Check(override val sender: ActorRef<Message>, val attempt: Array<Int>): Message