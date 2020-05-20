package message

import akka.actor.typed.ActorRef

class Stop(val actor: ActorRef<Message>): Message(actor)