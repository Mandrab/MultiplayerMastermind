package actor

import akka.actor.AbstractActor
import akka.actor.ReceiveTimeout
import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.Adapter
import akka.actor.typed.receptionist.Receptionist
import message.*
import kotlin.random.Random

class ArbiterActor: AbstractActor() {
    private val typedSelf = Adapter.toTyped<Message>(self)

    private var secretValueLength : Int= 0
    private var playerNumber: Int = 0
    private var turnNumber: Int = 1
    private var idPlayer : String  = ""
    private var tryWin: Boolean = false
    private var checkSecretNumber  = 0
    private var numberOfCheck : Int = 0
    private var turnArray: Array<Int> = arrayOf(0)
    private var index:Int = 0
    private lateinit var attempterPlayer : ActorRef<Message>
    private var waitingWin = false

    private lateinit var players: Map<String, ActorRef<Message>>

    override fun preStart() {
        super.preStart()
        Adapter.toTyped(context.system).receptionist().tell(Receptionist
                .register(Services.Service.START_GAME.key, Adapter.toTyped(self)))
    }

    private fun start(msg: StartGame) : Collection<ActorRef<Message>> {
        this.secretValueLength = msg.secretLength
        this.playerNumber = msg.playerCount
        this.turnArray = (0 until playerNumber).toList().toTypedArray()

        players = (0 until playerNumber).map {
            Pair("Player$it", Adapter.spawn(context, PlayerActor.create("Player$it", it), "Player$it")) }
                .toMap()
        //TODO ? Adapter.watch(context, player)
        return players.values.onEach { it.tell(StartGame(typedSelf, playerNumber, secretValueLength, players.values.toList())) }
    }

    private fun startGame(msg: StartGame) {
        start(msg).let { msg.sender.tell(GamePlayers(typedSelf, it.toList())) }      // tell to actor-view
        turn()
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(CheckTimeout::class.java) { timeout(it) }
                .match(StartGame::class.java) { msg -> startGame(msg) }
                .match(Guess::class.java) { msg -> guess(msg) }
                .match(CheckResult::class.java) { msg -> checkResult(attempterPlayer, msg.black, msg.white) }
                .match(Try::class.java) { msg -> tryWin(msg.attempt) }
                .match(ReceiveTimeout::class.java) { msg -> lostTurn(this.idPlayer) }
                .build()
    }

    private val timeout: (CheckTimeout) -> Unit = {
        Thread.sleep(5) //TODO remove
        if (it.idPlayer == idPlayer && it.turn == turnNumber && !waitingWin) {
            if (System.currentTimeMillis() - it.time > 5000) lostTurn(idPlayer)
            else self.tell(it, self)
        }
    }

    private fun guess(msg: Guess) {
        if (msg.attackerID == idPlayer) {
            players[msg.defenderID]
            val playerID = context.actorSelection(msg.defenderID)
            println("guess " + msg.defenderID + " " + playerID)
            players[msg.defenderID]?.tell(Check(typedSelf, msg.attempt))
            println("sned")
            attempterPlayer = msg.sender
        }
    }

    private fun checkResult(attempterPlayer: ActorRef<Message>, black: Int, white: Int) {
        println("checkresult")
        if (!this.tryWin) {
            consolePrint(black, white)
            waitingWin = true
            attempterPlayer.tell(WannaTry(typedSelf, this.turnNumber))
        } else checkWin(black, white)
    }

    private fun tryWin(attempt: Array<Array<Int>>?) {
        attempt?.let {
            val children = context.children
            this.tryWin = true
            children.forEachIndexed { i, c -> c.tell(Check(typedSelf, attempt[i]), self) }
        } ?: let {
            turn()
            tryWin = false
        }
    }

    private fun turn() {
        if (index == this.turnArray.size) {
            println("turn $turnNumber")
            turnNumber++
            turnArray = turnArray.sortedBy { Random.nextInt(playerNumber) }.toTypedArray()
            this.index = 0
        }
        waitingWin = false
        val selectPlayerTurn: Int = this.turnArray[this.index++]
        this.idPlayer = "Player$selectPlayerTurn"

        context.actorSelection(idPlayer).tell(ExecTurn(typedSelf, turnNumber), self)
        self.tell(CheckTimeout(idPlayer, turnNumber, System.currentTimeMillis()), self)
        /*
        val playerTurn = context.actorSelection(this.idPlayer)
        ...
        val timeout = Timeout.create(java.time.Duration.ofSeconds(5))
        val future = Patterns.ask(playerTurn, ExecTurn(typedSelf, turnNumber), timeout)

        try { // TODO
            Timer.schedule(
                delay: Long,
                crossinline action: TimerTask.() -> Unit
            ): TimerTask (source)
            Await.result(future, Duration.fromNanos(5*10.0.pow(9))).toString()
        }catch ( e:Exception ){
            lostTurn(this.idPlayer)
        }
        context.system.scheduler.scheduleOnce(Duration.ofMillis(10)!!, {
            TODO()
        }, context.system.dispatcher)*/
    }

    private fun consolePrint(correctPlace: Int, wrongPlace:Int){
        println("The $idPlayer guessed $correctPlace digits in rights place and $wrongPlace digits in a wrong place." )
    }

    private fun checkWin(correctPlace: Int, wrongPlace: Int) {
        this.numberOfCheck++ // rappresenta il numero di tentativi di vincita sottomessi
        if (correctPlace == this.secretValueLength && wrongPlace == 0) {
            this.checkSecretNumber++ // numero di tentativi di vincita corretti
        }
        if (this.numberOfCheck == this.playerNumber) {// se tutti i tentaivi sono stati sottomessi
            this.tryWin = false
            if (this.checkSecretNumber == this.numberOfCheck) {
                context.children.forEach { it.tell(End(typedSelf, this.idPlayer), self) }
            } else {
                context.children.forEach { it.tell(Ban(typedSelf, this.idPlayer), self) }
                turn()
            }
        }
    }

    private fun lostTurn(idPlayer : String) {
        println("The $idPlayer lost turn")
        context.actorSelection(idPlayer).tell(LostTurn(typedSelf, "Lost Turn"),self)
        turn()
    }

    private data class CheckTimeout(val idPlayer: String, val turn: Int, val time: Long)
}