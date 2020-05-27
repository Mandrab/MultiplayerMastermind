package algorithm

import kotlin.random.Random

/**
 * Represent a code, i.e.: a succession of numbers.
 * It also implements some utility methods.
 *
 * @author Paolo Baldini
 */
class Code(code: Array<Int> = Array(secretLength) { Random.nextInt(alphabetChars) }) {
    val code: IntArray = code.toIntArray()

    fun guess(attempt: Code): Result {
        val black = code.filterIndexed { idx, it -> it == attempt.code[idx] }.count()
        val white = (code.distinct().count { attempt.code.contains(it) } - black).coerceAtLeast(0)

        return Result(black, white)
    }

    override fun hashCode(): Int = code.hashCode()

    override fun equals(other: Any?) = other != null && other is Code && (0 until secretLength).all { code[it] == other.code[it] }

    override fun toString() = "Code: ${code.joinToString()}"

    companion object {
        var alphabetChars = 10
        var secretLength = 4

        fun codes(): Iterator<Code> = object : Iterator<Code> {
            private var nextCode = Code(Array(secretLength) { 0 })

            override fun hasNext() = nextCode != Code(Array(secretLength) { alphabetChars -1 })

            override fun next(): Code = nextCode.also { nextCode = next(nextCode) }
        }

        private fun next(code: Code): Code {
            val nextCode = code.code.toTypedArray()

            var idx = nextCode.size -1
            do {
                nextCode[idx] = (nextCode[idx] +1) % alphabetChars
            } while (idx > 0 && nextCode[idx--] == 0)

            return Code(nextCode)
        }
    }
}