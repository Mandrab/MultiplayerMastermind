package message

import akka.actor.typed.ActorRef

data class StartGame(val actor: ActorRef<Message>, val playerCount: Int, val secretValueLength: Int): Message(actor)