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
/*
    Code.alphabetChars = 6         // alphabet
    Code.secretLength = 12          // length

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
        breaker.update()
    } while (!response.isCorrect())
    println("win")
    println(System.currentTimeMillis() - time)
*/

    val system = ActorSystem.create("Mastermind")
    system.actorOf(Props.create(ArbiterActor::class.java), "Arbiter")
    val view = ViewImpl()
    view.actor = TypedActorSystem.wrap(system).systemActorOf(ViewActor.create(view), "ViewActor", TypedProps.empty())
}