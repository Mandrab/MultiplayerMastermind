package actor

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.*
import akka.actor.typed.receptionist.Receptionist
import algorithm.AttackerStrategy
import algorithm.Code
import algorithm.CodeMaker
import algorithm.Result
import message.*

class PlayerActor private constructor(
        context: ActorContext<Message>,
        private val playerID: String
) : AbstractBehavior<Message>(context), CodeMaker {
    private lateinit var playersStates: MutableMap<String, AttackerStrategy>
    private var lastAttack: Pair<String, Code>? = null

    override val secret by lazy { Code() }
    override fun verify(guess: Code) = secret.guess(guess)

    fun idle(): Behavior<Message> = Behaviors.receive(Message::class.java).onMessage(StartGame::class.java) { apply {
        Code.secretLength = it.secretLength
        playersStates = (0 until it.playerCount).map { Pair("Player$it", AttackerStrategy()) }.toMap().toMutableMap()
    } }.build()

    override fun createReceive(): Receive<Message> = newReceiveBuilder()
            .onMessage(Ban::class.java, ban)
            .onMessage(StopGame::class.java) { Behaviors.stopped() }
            .onMessage(End::class.java) { Behaviors.stopped() }
            .onMessage(ExecTurn::class.java, execTurn)
            .onMessage(Check::class.java, check)
            .onMessage(CheckResult::class.java, checkResult)
            .onMessage(Services.Broadcast::class.java, broadcast)
            .onMessage(WannaTry::class.java, wannaTry)
            .onMessage(Update::class.java, update)
            .build()

    private val execTurn: (ExecTurn) -> Behavior<Message> = { exec -> also {
        if (playersStates.filterNot { it.key == playerID }.all { it.value.found }) {
            exec.sender.tell(Try(context.self, exec.turn, playersStates.map {
                if (it.key == playerID) secret.code.toTypedArray()
                else it.value.makeAttempt().code.toTypedArray() }.toTypedArray()))
        } else {
            playersStates.filterNot { it.key == playerID || it.value.found }.entries.firstOrNull { it.value.ready }?.let {
                val attempt = it.value.makeAttempt()
                lastAttack = Pair(it.key, attempt)
                exec.sender.tell(Guess(context.self, exec.turn, attempt.code.toTypedArray(), playerID, it.key))
            }
        }
    } }

    private val check: (Check) -> Behavior<Message> = { check -> also {
        val result = secret.guess(Code(check.attempt))
        val checkResult = CheckResult(context.self, result.black, result.white, check.attackerID, check.defenderID) // TODO check mainreceiver

        Services.broadcastList(Services.Service.OBSERVE_RESULT.key, context, checkResult)
        check.sender.tell(checkResult)
    } }

    private val checkResult: (CheckResult) -> Behavior<Message> = { result -> also {
        if (result.attackerID == playerID) lastAttack?.let {
            playersStates[it.first]?.attemptResult(it.second, Result(result.black, result.white))
            lastAttack = null
            context.self.tell(Update())
        }
    } }

    private val broadcast: (Services.Broadcast) -> Behavior<Message> = { apply {
        it.actors.foreach { act -> act.tell(it.msg) }
    } }

    private val wannaTry: (WannaTry) -> Behavior<Message> = { result -> also {
        if (playersStates.filterNot { it.key == playerID }.values.all { it.found }) {
            result.sender.tell(Try(context.self, result.turn, playersStates.map {
                if (it.key == playerID) secret.code.toTypedArray()
                else it.value.makeAttempt().code.toTypedArray() }.toTypedArray()))
        } else result.sender.tell(Try(context.self, result.turn, null))
    } }

    private val update: (Update) -> Behavior<Message> = { also {
        playersStates.values.firstOrNull { !it.ready }?.let {
            it.tickUpdate()
            context.self.tell(Update())
        }
    } }

    private val ban: (Ban) -> Behavior<Message> = {
        if (it.playerID == playerID) {
            Behaviors.stopped()
        } else {
            playersStates.remove(it.playerID)
            this@PlayerActor
        }
    }

    companion object {
        fun create(ID: String): Behavior<Message> = Behaviors.setup {
            it.system.receptionist().tell(Receptionist.register(Services.Service.OBSERVE_RESULT.key, it.self))
            PlayerActor(it, ID).idle()
        }
    }

    private inner class Update: Message { override val sender: ActorRef<Message> = context.self }
}