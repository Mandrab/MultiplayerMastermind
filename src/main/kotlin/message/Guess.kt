package message
import akka.actor.typed.ActorRef

data class Guess(
        override val sender: ActorRef<Message>,
        val turn: Int,
        val attempt: Array<Int>,
        val playerID: String
): Message {

    override fun equals(other: Any?): Boolean {
        /*if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Guess

        if (turn != other.turn) return false
        if (!attempt.contentEquals(other.attempt)) return false
        if (playerID != other.playerID) return false

        return true*/TODO()
    }

    override fun hashCode(): Int {
        var result = turn
        result = 31 * result + attempt.contentHashCode()
        result = 31 * result + playerID.hashCode()
        return result
    }
}