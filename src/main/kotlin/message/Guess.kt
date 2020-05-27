package message

import akka.actor.typed.ActorRef

data class Guess(
        override val sender: ActorRef<Message>,
        val turn: Int,
        val attempt: Array<Int>,
        val attackerID: String,
        val defenderID: String
): Message