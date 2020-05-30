package message

import akka.actor.typed.ActorRef

data class StartGame(
        override val sender: ActorRef<Message>,
        val playerCount: Int,
        val secretLength: Int,
        val humanPlayer: ActorRef<Message>?,
        val players: List<ActorRef<Message>>
): Message
