package message

import akka.actor.typed.ActorRef

enum class Distance {
    NOT_IN_SECRET,
    WRONG_PLACE,
    CORRECT_PLACE
}

data class CheckResult(val actor: ActorRef<Message>, val correctPlace: Int, val wrongPlace: Int): Message(actor)