package message

import akka.actor.typed.ActorRef

data class StartGame(override val sender: ActorRef<Message>, val playerCount: Int, val secretValueLength: Int): Message