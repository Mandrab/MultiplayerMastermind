package message

import akka.actor.typed.ActorRef

/**
 * This class represent a Guess message.
 *
 * @param sender, sender of message
 * @param turn, represent number of turn
 * @param attempt, number of attempt
 * @param attackerID, player who send message
 * @param defenderID, player who made guess.
 *
 * @author Baldini Paolo, Battistini Ylenia
 */
data class Guess(
        override val sender: ActorRef<Message>,
        val turn: Int,
        val attempt: Array<Int>,
        val attackerID: String,
        val defenderID: String
): Message