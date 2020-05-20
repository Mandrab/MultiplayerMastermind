import agent.PlayerAgent
import akka.actor.typed.ActorSystem
import akka.actor.typed.Props
import message.Message

fun main() {
    val system: ActorSystem<Message> = ActorSystem.create(PlayerAgent.create("PlayerID", 5, 5), "player")
}