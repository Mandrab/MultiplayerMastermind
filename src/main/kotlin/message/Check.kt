package message

import akka.actor.typed.ActorRef

data class Check(override val sender: ActorRef<Message>, val attempt: Array<Int>): Message {
    override fun equals(other: Any?): Boolean {
        /*if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Guess

        if (!attempt.contentEquals(other.attempt)) return false

        return true*/ TODO()
    }

    override fun hashCode(): Int {
        return attempt.contentHashCode()
    }
}