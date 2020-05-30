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
) : AbstractPlayerActor(context), CodeMaker {
    private lateinit var playersStates: MutableMap<String, AttackerStrategy>
    private var lastAttack: Pair<String, Code>? = null

    override val secret by lazy { Code() }
    override fun verify(guess: Code) = secret.guess(guess)

    fun idle(): Behavior<Message> = Behaviors.receive(Message::class.java).onMessage(StartGame::class.java) { apply {
        Code.secretLength = it.secretLength
        playersStates = (0 until it.playerCount).map { Pair("Player$it", AttackerStrategy()) }.toMap().toMutableMap()
    } }.build()

    override val execTurn: (ExecTurn) -> Behavior<Message> = { exec -> also {
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

    override val checkResult: (CheckResult) -> Behavior<Message> = { result -> also {
        if (result.attackerID == playerID) lastAttack?.let {
            playersStates[it.first]?.attemptResult(it.second, Result(result.black, result.white))
            lastAttack = null
            context.self.tell(Update())
        }
    } }

    override val wannaTry: (WannaTry) -> Behavior<Message> = { result -> also {
        if (playersStates.filterNot { it.key == playerID }.values.all { it.found }) {
            result.sender.tell(Try(context.self, result.turn, playersStates.map {
                if (it.key == playerID) secret.code.toTypedArray()
                else it.value.makeAttempt().code.toTypedArray() }.toTypedArray()))
        } else result.sender.tell(Try(context.self, result.turn, null))
    } }

    override val lostTurn: (LostTurn) -> Behavior<Message> = { this }

    override val ban: (Ban) -> Behavior<Message> = {
        if (it.playerID == playerID) {
            Behaviors.stopped()
        } else {
            playersStates.remove(it.playerID)
            this@PlayerActor
        }
    }

    override val stopGame: (StopGame) -> Behavior<Message> = { Behaviors.stopped() }

    override val end: (End) -> Behavior<Message> = { Behaviors.stopped() }

    override val onAny: (Message) -> Behavior<Message> = { apply {
        if (it is Update) playersStates.values.firstOrNull { !it.ready }?.let {
            it.tickUpdate()
            context.self.tell(Update())
        }
    } }

    companion object {
        fun create(ID: String): Behavior<Message> = Behaviors.setup {
            it.system.receptionist().tell(Receptionist.register(Services.Service.OBSERVE_RESULT.key, it.self))
            PlayerActor(it, ID).idle()
        }
    }

    private inner class Update: Message { override val sender: ActorRef<Message> = context.self }
}