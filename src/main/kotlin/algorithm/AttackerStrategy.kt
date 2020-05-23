package algorithm

/**
 * This class implements an algorithm that focus on speed and memory usage instead of use the minimum number of moves.
 *
 * @author Paolo Baldini
 */
class AttackerStrategy : Challenger {
    private val previousResults = mutableListOf<Pair<Code, Result>>()
    private val iterator = Code.codes()
    private var attempt: Code = Code()

    var found = false
        private set

    var ready = true
        private set

    override fun makeAttempt(): Code = attempt.also { ready = false }

    override fun attemptResult(response: Result) {
        found = response.isCorrect()
        if (found) return

        previousResults.add(Pair(attempt, response))

        check(iterator.hasNext())

        attempt = iterator.next()
        while (previousResults.any { it.first.guess(attempt) != it.second } && iterator.hasNext())
            attempt = iterator.next()

        ready = true
    }

    fun tickSearch(iterations: Int = 1) {
        if (found) return
        check(iterator.hasNext())

        var i = iterations

        attempt = iterator.next()
        while (i-- > 0 && previousResults.any { it.first.guess(attempt) != it.second } && iterator.hasNext())
            attempt = iterator.next()

        ready = previousResults.any { it.first.guess(attempt) != it.second }
    }
}