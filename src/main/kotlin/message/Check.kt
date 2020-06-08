package message

import akka.actor.typed.ActorRef

/**
 * This class represent a Check message.
 *
 * @param sender, sender of message
 * @param attempt, attempted code
 * @param attackerID, player who send message
 * @param defenderID, player i want to guess
 * @param turn, represent number of turn.
 *
 * @author Baldini Paolo, Battistini Ylenia
 */
data class Check(
        override val sender: ActorRef<Message>,
        val attempt: Array<Int>,
        val attackerID: String,
        val defenderID: String,
        val turn: Int
): Message