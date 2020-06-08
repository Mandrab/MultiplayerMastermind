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

/**
 * This class represent a PlayerActor and implements AbstractPlayerActor class.
 * PlayerActor has a playerID that identifies it.
 * First, when the player is created, it defines its secret number using the Code class.
 *
 * @author Baldini Paolo, Battistini Ylenia
 */
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

    /**
     * Execute a turn
     *
     * if guessed all the players codes, then send Try message to ArbiterActor
     * else send a Guess message.
     */
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

    /**
     * Check if result message is addressed to him
     * In that case, set number of digits guessed in the right
     * place and the number of digits guessed in the wrong place.
     */
    override val checkResult: (CheckResult) -> Behavior<Message> = { result -> also {
        if (result.attackerID == playerID) lastAttack?.let {
            playersStates[it.first]?.attemptResult(it.second, Result(result.black, result.white))
            lastAttack = null
            context.self.tell(Update())
        }
    } }

    /**
     * if the player wants to try to win send Try message containing the secret values for all players
     * if instead he does not want, send a Try message with null attempt.
     */
    override val wannaTry: (WannaTry) -> Behavior<Message> = { result -> also {
        if (playersStates.filterNot { it.key == playerID }.values.all { it.found }) {
            result.sender.tell(Try(context.self, result.turn, playersStates.map {
                if (it.key == playerID) secret.code.toTypedArray()
                else it.value.makeAttempt().code.toTypedArray() }.toTypedArray()))
        } else result.sender.tell(Try(context.self, result.turn, null))
    } }

    override val lostTurn: (LostTurn) -> Behavior<Message> = { this }

    /**
     * if the playerID is equal to his then he blocks his behavior,
     * if not, he delete the banned player from the list of players.
     */
    override val ban: (Ban) -> Behavior<Message> = {
        if (it.playerID == playerID) {
            Behaviors.stopped()
        } else {
            playersStates.remove(it.playerID)
            this@PlayerActor
        }
    }

    /**
     * When player receives a StopGame message he blocks his behaviour.
     */
    override val stopGame: (StopGame) -> Behavior<Message> = { Behaviors.stopped() }

    /**
     * When player receives a End message he blocks his behaviour.
     */
    override val end: (End) -> Behavior<Message> = { Behaviors.stopped() }

    override val onAny: (Message) -> Behavior<Message> = { apply {
        if (it is Update) playersStates.values.firstOrNull { !it.ready }?.let {
            it.tickUpdate()
            context.self.tell(Update())
        }
    } }

    /**
     * The player registers to be able to receive attempts results.
     */
    companion object {
        fun create(ID: String): Behavior<Message> = Behaviors.setup {
            it.system.receptionist().tell(Receptionist.register(Services.Service.OBSERVE_RESULT.key, it.self))
            PlayerActor(it, ID).idle()
        }
    }

    /**
     * Self message to execute long running tasks
     */
    private inner class Update: Message { override val sender: ActorRef<Message> = context.self }
}