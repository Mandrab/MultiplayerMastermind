package message

import akka.actor.typed.ActorRef

/*
This class represent a WannaTry message.
@param sender, sender of message
@param turn, represent number of turn.

@author Baldini Paolo, Battistini Ylenia
*/
data class WannaTry(override val sender: ActorRef<Message>, val turn: Int): Message
