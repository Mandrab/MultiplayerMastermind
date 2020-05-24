package message

import akka.actor.typed.ActorRef

data class Try(override val sender: ActorRef<Message>, val turn: Int, val attempt: Array<Array<Int>>?): Message