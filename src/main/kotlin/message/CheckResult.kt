package message

import akka.actor.typed.ActorRef

/**
 * This class represent a CheckResult message.
 *
 * @param sender, sender of message
 * @param black, digits in correct place
 * @param white, digits in wrong place
 * @param attackerID, player who attempted the code
 * @param defenderID, player who made guess
 * @param turn, represent number of turn.
 *
 * @author Baldini Paolo, Battistini Ylenia
 */
data class CheckResult(
        override val sender: ActorRef<Message>,
        val black: Int,
        val white: Int,
        val attackerID: String,
        val defenderID: String,
        val turn: Int
): Message