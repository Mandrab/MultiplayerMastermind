package message

import akka.actor.typed.ActorRef

/**
 * This class represent a End message.
 *
 * @param sender, sender of message
 * @param winnerID, player who won.
 *
 * @author Baldini Paolo, Battistini Ylenia
 */
data class End(override val sender: ActorRef<Message>, val winnerID: String): Message
