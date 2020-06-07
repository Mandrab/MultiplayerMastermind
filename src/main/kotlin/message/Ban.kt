package message

import akka.actor.typed.ActorRef

/*
This class represent a Ban message.
@param sender, sender of message
@param palyerID, id of player

@author Baldini Paolo, Battistini Ylenia
 */
data class Ban(override val sender: ActorRef<Message>, val playerID: String): Message