package algorithm

/**
 * Contains count of digits with known position (black) and count of digits in secret
 * code of which position is unknown (white).
 *
 * @author Paolo Baldini
 */
class Result(val black: Int, val white: Int) {

    fun isCorrect() = black == Code.secretLength

    override fun hashCode(): Int {
        var hash = 7
        hash = 13 * hash + black
        hash = 13 * hash + white
        return hash
    }

    override fun equals(other: Any?) = other != null && other is Result && black == other.black && white == other.white

    override fun toString(): String = "black=$black, white=$white"
}