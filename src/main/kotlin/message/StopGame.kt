package message

import akka.actor.typed.ActorRef

/**
 * This class represent a StopGame message.
 * @param sender, sender of message.
 *
 * @author Baldini Paolo, Battistini Ylenia
 */
class StopGame(override val sender: ActorRef<Message>): Message
