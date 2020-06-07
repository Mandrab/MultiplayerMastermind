package message

import akka.actor.typed.ActorRef

/*
This is a Message Interface.
All messages implements it.
 */
interface Message {
    val sender: ActorRef<Message>
}