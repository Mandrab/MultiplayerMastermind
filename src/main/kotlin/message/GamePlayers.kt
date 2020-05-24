package message

import akka.actor.typed.ActorRef

data class GamePlayers(override val sender: ActorRef<Message>, val players: List<ActorRef<Message>>) : Message
