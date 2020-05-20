package message

import akka.actor.typed.ActorRef

abstract class Message(val sender: ActorRef<Message>)