package algorithm

import kotlin.math.pow
import kotlin.random.Random

/**
 * Represent a code, i.e.: a succession of numbers.
 * It also implements some utility methods.
 *
 * @author Paolo Baldini
 */
class Code(code: Int = Random.nextInt(solutionsCount)) {
    private val code = (0 until secretLength).map { code.div(10.0.pow(secretLength -it -1)).toInt() % 10 }.toIntArray()

    fun guess(attempt: Code): Result {
        val black = code.filterIndexed { idx, it -> it == attempt.code[idx] }.count()
        val white = (code.distinct().count { attempt.code.contains(it) } - black).coerceAtLeast(0)

        return Result(black, white)
    }

    override fun hashCode(): Int = code.hashCode()

    override fun equals(other: Any?) = other != null && other is Code && code.asList().containsAll(other.code.asList())

    override fun toString() = "Code: ${code.joinToString("")}"

    companion object {
        var alphabetChars = 6
            set(value) { field = value; update() }
        var secretLength = 4
            set(value) { field = value; update() }

        lateinit var codes: Set<Code>

        private var solutionsCount: Int = alphabetChars.toDouble().pow(secretLength).toInt()

        private fun update() {
            solutionsCount = alphabetChars.toDouble().pow(secretLength).toInt()
            codes = (0..solutionsCount).map { Code(it) }.toSet()
        }
    }
}