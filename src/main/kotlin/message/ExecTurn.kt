package message

import akka.actor.typed.ActorRef

data class ExecTurn(val actor: ActorRef<Message>, val turn: Int): Message(actor)