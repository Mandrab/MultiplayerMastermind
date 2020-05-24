import algorithm.AttackerStrategy
import algorithm.Code
import algorithm.CodeMakerImpl

import actor.ArbiterActor
import actor.ViewActor
import akka.actor.ActorSystem
import akka.actor.Props
import message.Message
import view.ViewImpl

typealias TypedActorSystem = akka.actor.typed.ActorSystem<Message>
typealias TypedProps = akka.actor.typed.Props

fun main() {
/*    val system: ActorSystem = ActorSystem.create("Mastermind")
    val typedSystem = akka.actor.typed.ActorSystem.wrap(system)
    val player = typedSystem.systemActorOf(PlayerAgent.create("PlayerID", 5, 5), "player", Props.empty())

    player.tell(StartGame(typedSystem.ignoreRef(), 1, 4))
    val breaker = AttackerStrategy()

    val time = System.currentTimeMillis()

    var turn = 0
    do {
        println("\n========Turn " + ++turn + "========")
        val guess = breaker.makeAttempt()
        val response = player.tell(Check())
        println("Response: $response")
        breaker.attemptResult(response)
    } while (!response.isCorrect())

    println(System.currentTimeMillis() - time)*/
    //ViewImpl()

   /* Code.alphabetChars = 6         // alphabet
    Code.secretLength = 10          // length

    val maker = CodeMakerImpl()
    val breaker = AttackerStrategy()

    val time = System.currentTimeMillis()

    var turn = 0
    do {
        println("\n========Turn " + ++turn + "========")
        val guess = breaker.makeAttempt()
        println(guess)
        val response = maker.verify(guess)
        println("Response: $response")
        breaker.attemptResult(response)
    } while (!response.isCorrect())

<<<<<<< HEAD
    println(System.currentTimeMillis() - time)
=======
    val system = ActorSystem.create("Mastermind")
    val arbiter = system.actorOf(Props.create(ArbiterActor::class.java), "arbiter")
>>>>>>> origin/arbiterAgent*/

    val system = ActorSystem.create("Mastermind")
    system.actorOf(Props.create(ArbiterActor::class.java), "arbiter")
    val view = ViewImpl()
    view.actor = TypedActorSystem.wrap(system).systemActorOf(ViewActor.create(view), "ViewActor", TypedProps.empty())
}