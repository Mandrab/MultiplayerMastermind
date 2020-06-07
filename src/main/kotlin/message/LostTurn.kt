package message

import akka.actor.typed.ActorRef

/*
This class represent a LostTurn message.
@param sender, sender of message
@param attackerID, player who lost turn
@param turn, represent number of turn.

@author Baldini Paolo, Battistini Ylenia
*/
data class LostTurn(override val sender: ActorRef<Message>, val attackerID: String, val turn:Int) : Message