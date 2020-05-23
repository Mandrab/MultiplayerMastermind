package message

import akka.actor.typed.ActorRef

data class CheckResult(override val sender: ActorRef<Message>, val correctPlace: Int, val wrongPlace: Int, val mainReceiver: ActorRef<Message>): Message