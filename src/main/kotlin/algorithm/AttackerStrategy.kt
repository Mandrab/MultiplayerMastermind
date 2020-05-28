package algorithm

import kotlin.math.pow

/**
 * This class implements an algorithm that focus on speed and memory usage instead of use the minimum number of moves.
 *
 * @author Paolo Baldini
 */
class AttackerStrategy : FaultChallenger {
    private val previousResults = mutableSetOf<Pair<Code, Result>>()
    private val iterator = Code.codes()
    private var nextAttempt: Code = Code()

    var found = false
        private set

    var ready = true
        private set

    override fun makeAttempt(): Code = nextAttempt.also { ready = false || found }

    override fun attemptResult(attempt: Code, response: Result) = previousResults.add(Pair(attempt, response)).run {
        found = response.isCorrect()
        ready = ready || found
    }

    fun update() {
        if (found) return

        do nextAttempt = iterator.next()
        while (previousResults.any { it.first.guess(nextAttempt) != it.second } && iterator.hasNext())

        ready = true
    }

    fun tickUpdate(iterations: Int = 10, multiplier: Int = 5) {
        if (found) return

        var i = iterations.toDouble().pow(multiplier).toInt()

        do nextAttempt = iterator.next()
        while (i-- > 0 && previousResults.any { it.first.guess(nextAttempt) != it.second } && iterator.hasNext())

        ready = previousResults.all { it.first.guess(nextAttempt) == it.second }
    }
}