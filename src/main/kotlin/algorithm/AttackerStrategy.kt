package algorithm

/**
 * This class implements an algorithm, inspired by the 'Five-guess algorithm' to solve the mastermind game.
 * It doesn't follow exactly the aforesaid algorithm, but reach anyways a solution.
 *
 * @author Paolo Baldini
 */
class AttackerStrategy : Challenger {
    private val possible: MutableSet<Code> = Code.codes.toMutableSet()
    private var attempt: Code = Code(1122)

    override fun makeAttempt(): Code = attempt

    override fun attemptResult(response: Result) {
        if (response.isCorrect()) possible.apply { clear() }.add(attempt)
        else {
            possible.removeIf { attempt.guess(it) != response }

            val bestGuesses = Code.codes.toList().sortedBy { code ->
                val minMaxTable = (0..Code.secretLength).map { IntArray(Code.secretLength +1 -it) }

                possible.map { code.guess(it) }.forEach { minMaxTable[it.black][it.white]++ }

                possible.size - minMaxTable.maxBy { it.max()!! }!!.max()!!
            }

            attempt = bestGuesses.findLast { possible.contains(it) } ?: bestGuesses.last()
        }
    }
}