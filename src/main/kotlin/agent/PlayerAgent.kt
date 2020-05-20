package agent

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import message.*

class PlayerAgent private constructor(
        context: ActorContext<Message>,
        private val ID: String,
        private val playerNum: Int,
        private val valueSize: Int
): AbstractBehavior<Message>(context) {
    companion object {
        fun create(ID: String, players: Int, secret: Int): Behavior<Message> = Behaviors.setup {
            PlayerAgent(it, ID, players, secret).idle()
        }
    }
    private val secret = IntArray(valueSize) { (0..9).random() }

    fun idle(): Behavior<Message> = newReceiveBuilder().onMessage(StartGame::class.java) { this }.build()

    override fun createReceive(): Receive<Message> = newReceiveBuilder()
            .onMessage(Ban::class.java) { Behaviors.stopped() }
            .onMessage(Stop::class.java) { Behaviors.stopped() }
            .onMessage(End::class.java) { Behaviors.stopped() }
            .onMessage(ExecTurn::class.java, execTurn)
            .onMessage(Check::class.java, check)
            .onMessage(CheckResult::class.java, checkResult)
            .onMessage(WannaTry::class.java, wannaTry)
            .build()

    private val execTurn: (ExecTurn) -> Behavior<Message> = { exec -> also {
        // TODO prepare guess
        val attempt = Guess(context.self, exec.turn, emptyArray(), ID)
        exec.sender.tell(attempt)
    } }

    private val check: (Check) -> Behavior<Message> = { check -> also {
        val numberInCorrectPlace = (0..valueSize).count { check.attempt[it] == secret[it] }
        val numberInWrongPlace = check.attempt.filterIndexed { idx, elem -> elem != secret[idx] }.groupBy { it }
                .map { Pair(it.key, it.value.count()) }
                .map { pair -> pair.second.coerceAtMost(secret.filter { it == pair.first }.count()) }.sum()
        check.sender.tell(CheckResult(context.self, numberInCorrectPlace, numberInWrongPlace))
    } }

    private val checkResult: (CheckResult) -> Behavior<Message> = { result -> also {
        // TODO update result
    } }

    private val wannaTry: (WannaTry) -> Behavior<Message> = { result -> also {
        // TODO update result
    } }
}