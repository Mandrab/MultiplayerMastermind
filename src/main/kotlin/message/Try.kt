package message

data class Try(val turn: Int, val attempt: Array<Array<Int>>?){
    override fun equals(other: Any?): Boolean {
        /*if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Try

        if (turn != other.turn) return false
        if (attempt != null) {
            if (other.attempt == null) return false
            if (!attempt.contentDeepEquals(other.attempt)) return false
        } else if (other.attempt != null) return false

        return true*/ TODO()
    }

    override fun hashCode(): Int {
        var result = turn
        result = 31 * result + (attempt?.contentDeepHashCode() ?: 0)
        return result
    }
}