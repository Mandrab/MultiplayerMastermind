package actor

import akka.actor.AbstractActor
import akka.actor.ReceiveTimeout
import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.Adapter
import akka.actor.typed.receptionist.Receptionist
import akka.pattern.Patterns
import akka.util.Timeout
import message.*
import scala.concurrent.Await
import java.time.Duration


class ArbiterActor: AbstractActor() {
    private val typedSelf = Adapter.toTyped<Message>(self)

    private var secretValueLength : Int= 0
    private var playerNumber: Int = 0
    private var turnNumber: Int = 1
    private var idPlayer : String  = " "
    private var effectivePlayerTurn: Int = 0
    private var tryWin: Boolean = false
    private var checkSecretNumber  = 0
    private var numberOfCheck : Int = 0
    private var turnArray: Array<Int> = arrayOf(0)
    private var index:Int = 0
    lateinit var playerX : ActorRef<Message>

    override fun preStart() {
        super.preStart()
        Adapter.toTyped(context.system).receptionist().tell(Receptionist
                .register(Services.Service.START_GAME.key, Adapter.toTyped(self)))
    }

    private fun start(msg: StartGame) : List<ActorRef<Message>> {
        this.secretValueLength = msg.secretLength
        this.playerNumber = msg.playerCount
        this.turnArray = (0 until playerNumber).toList().toTypedArray()

        val players = (0 until playerNumber).map {
            Adapter.spawn(context, PlayerActor.create("Player$it", it), "Player$it") }
        //TODO ? Adapter.watch(context, player)
        return players.onEach { it.tell(StartGame(typedSelf, playerNumber, secretValueLength, players)) }
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(StartGame::class.java) { msg -> startGame(msg) }
                .match(Guess::class.java){ msg -> guess(msg) }
                .match(CheckResult::class.java) { msg -> checkResult(playerX, msg.black, msg.white) }
                .match(Try::class.java){ msg -> tryWin(msg.attempt) }
                .match(ReceiveTimeout::class.java) { msg -> lostTurn(this.idPlayer) }
                .build()
    }

    private fun startGame(msg: StartGame) {
        start(msg).let { msg.sender.tell(GamePlayers(typedSelf, it)) }
        turn()
    }

    private fun guess(msg: Guess) {
        if (msg.playerID.equals(this.idPlayer)) {
            val playerID = context.actorSelection(msg.playerID);
            playerID.tell(Check(typedSelf, msg.attempt), self)
            this.idPlayer = msg.playerID
            System.out.println(msg.sender)
            playerX = msg.sender
        } else //TODO: forse si può evitare semplicemente non lo gestisco. Tanto arrriva solo a me.
         context.children.forEach { it.tell(NotCheck(typedSelf, msg.playerID, msg.turn), self) }
    }

    private fun checkResult(playerX: ActorRef<Message>, black: Int, white: Int) {
        System.out.println(playerX)
        if (!this.tryWin) {
            consolePrint(black, white)
            playerX.tell(WannaTry(typedSelf, this.turnNumber))
        } else checkWin(black, white)

    }

    private fun tryWin(attempt: Array<Array<Int>>?) {
        if(attempt != null) {
            var children = context.children
            var i = -1
            this.tryWin = true
            children.forEach { i++; it.tell(Check(typedSelf, attempt[i]), self) }
        }else {
            turn()
            tryWin = false
        }
    }

    private fun turn(){
        var selectPlayerTurn: Int
        if (this.index == this.turnArray.size-1) {
            this.turnArray.sortedArray()
            this.index = 0
        }
        selectPlayerTurn = this.turnArray.get(this.index)
        this.index++
        this.idPlayer = "Player" + selectPlayerTurn
        val playerTurn = context.actorSelection(this.idPlayer)

        val timeout = Timeout.create(Duration.ofSeconds(5))
        val future = Patterns.ask(playerTurn, ExecTurn(typedSelf, turnNumber), timeout)
         try {
             Await.result(future, timeout.duration()).toString()
         }catch ( e:Exception ){
             lostTurn(this.idPlayer)
         }
        /*context.system.scheduler.scheduleOnce(Duration.ofMillis(10)!!, {
            TODO()
        }, context.system.dispatcher)*/

        this.effectivePlayerTurn++
        if ( this.effectivePlayerTurn == this.playerNumber)  turnNumber++
    }

    private fun consolePrint(correctPlace: Int, wrongPlace:Int){
        System.out.println("The "+ this.idPlayer + " guessed " + correctPlace + " digits in rights place and " + wrongPlace + " digits in a wrong place." )
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
            }else {
                context.children.forEach { it.tell(Ban(typedSelf, this.idPlayer), self) }
                turn()
            }
        }
    }

    private fun lostTurn(idPlayer : String) {
        System.out.println("The "+ idPlayer + "lost turn")
        context.actorSelection(idPlayer).tell(LostTurn(typedSelf, "Lost Turn"),self)
        turn()
    }

}