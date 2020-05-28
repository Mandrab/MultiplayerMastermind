package message

import akka.actor.typed.ActorRef

class StopGame(override val sender: ActorRef<Message>): Message
