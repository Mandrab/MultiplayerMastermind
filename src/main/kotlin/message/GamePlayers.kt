package message

import akka.actor.typed.ActorRef

/*
This class represent a GamePlayers
@param sender, sender of message
@param players, list of ActorRef

@author Baldini Paolo, Battistini Ylenia
*/
data class GamePlayers(override val sender: ActorRef<Message>, val players: List<ActorRef<Message>>) : Message
