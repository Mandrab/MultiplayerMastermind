package message

import akka.actor.typed.ActorRef

/*
This class represent a StartGame message.
@param sender, sender of message
@param playerCount, number of player
@param secretLength, length of secret number
@param humanPlayer, ActorRef of humanPlayer. If human player not exist the ActorRef is null
@param players, list of ActorRef for all players.

@author Baldini Paolo, Battistini Ylenia
 */
data class StartGame(
        override val sender: ActorRef<Message>,
        val playerCount: Int,
        val secretLength: Int,
        val humanPlayer: ActorRef<Message>?,
        val players: List<ActorRef<Message>>
): Message
