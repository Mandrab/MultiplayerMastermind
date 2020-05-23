import agent.ArbiterAgent
import akka.actor.ActorSystem
import akka.actor.Props

fun main() {

    val system = ActorSystem.create("Mastermind")
    val arbiter = system.actorOf(Props.create(ArbiterAgent::class.java), "arbiter")
}