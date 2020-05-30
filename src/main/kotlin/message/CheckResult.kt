package message

import akka.actor.typed.ActorRef

data class CheckResult(
        override val sender: ActorRef<Message>,
        val black: Int,             // correctPlace
        val white: Int,             // wrongPlace
        val attackerID: String,
        val defenderID: String,
        val turn: Int
): Message