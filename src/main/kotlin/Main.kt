import algorithm.AttackerStrategy
import algorithm.Code
import algorithm.CodeMakerImpl

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

    Code.alphabetChars = 6         // alphabet
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

    println(System.currentTimeMillis() - time)
}