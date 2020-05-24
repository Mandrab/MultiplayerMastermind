package message

import akka.actor.typed.ActorRef

class Stop(override val sender: ActorRef<Message>): Message
