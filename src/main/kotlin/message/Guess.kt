package message

import akka.actor.typed.ActorRef

data class Guess(
        override val sender: ActorRef<Message>,
        val turn: Int,
        val attempt: Array<Int>,
        val playerID: String
): Message