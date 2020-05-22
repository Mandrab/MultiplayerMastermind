package algorithm

import java.util.concurrent.Executors

/**
 * This class implements an algorithm that focus on speed and memory usage instead of use the minimum number of moves.
 *
 * @author Paolo Baldini
 */
class AttackerStrategy : Challenger {
    private val previousResults = mutableListOf<Pair<Code, Result>>()
    private val iterator = Code.codes()
    private var attempt: Code = Code()

    override fun makeAttempt(): Code = attempt

    override fun attemptResult(response: Result) {
        if (response.isCorrect()) println("Won!")
        else {
            previousResults.add(Pair(attempt, response))

            check(iterator.hasNext())

            attempt = iterator.next()

            val time = System.currentTimeMillis()

            while (previousResults.any { it.first.guess(attempt) != it.second } && iterator.hasNext()) {
                attempt = iterator.next()
            }

            println(System.currentTimeMillis() - time)
        }
    }
}