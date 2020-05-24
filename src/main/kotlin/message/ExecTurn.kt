package message

import akka.actor.typed.ActorRef

data class ExecTurn(override val sender: ActorRef<Message>, val turn: Int): Message
