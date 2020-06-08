package message

import akka.actor.typed.ActorRef

/**
 * This class represent a Try message.
 * @param sender, sender of message
 * @param turn, represent number of turn
 * @param attempt, contains an attempt foreach player.
 *
 * @author Baldini Paolo, Battistini Ylenia
 */
data class Try(override val sender: ActorRef<Message>, val turn: Int, val attempt: Array<Array<Int>>?): Message