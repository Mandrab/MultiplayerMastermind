package message

import akka.actor.typed.ActorRef

/**
 * This is Message Interface.
 * All messages must implements it.
 */
interface Message {
    val sender: ActorRef<Message>
}