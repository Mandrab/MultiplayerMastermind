package agent

import akka.actor.*
import message.*
import java.time.Duration
import kotlin.time.milliseconds

class ArbiterAgent: AbstractActor() {

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

    private fun start(msg:StartGame){
        this.secretValueLength = msg.secretValueLength
        this.playerNumber = msg.playerCount
        this.turnArray = arrayOf(this.playerNumber)
        for( i in 1..this.playerNumber){
            val player = context.actorOf(Props.create(PlayerAgent::class.java), "Player" + i);
            player.tell(StartMsg(this.secretValueLength), self);
        }
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(StartGame::class.java) { msg ->
                    start(msg)
                    turn()
                }
                .match(Guess::class.java){ msg ->
                    if (msg.playerID.equals(this.idPlayer)) {
                        val playerID = context.actorSelection(msg.playerID);
                        playerID.tell(Check(msg.attempt), self)
                        this.idPlayer = msg.playerID
                        sender.tell(WannaTry(this.turnNumber), self)
                    }else context.children.forEach{it.tell(NotCheck(msg.playerID, msg.turn),self)}
                }
                .match(CheckResult::class.java) { msg ->
                    if (!this.tryWin) consolePrint(msg.correctPlace, msg.wrongPlace) else checkWin(msg.correctPlace, msg.wrongPlace)
                }
                .match(Try::class.java){ msg ->
                    //TODO: verificare cosa torna Paul
                    var children = context.children
                    var i = -1
                    children.forEach{i++; it.tell(Check(msg.attempt!![i]), self)}
                    this.tryWin = true
                }
                .match(ReceiveTimeout::class.java) {msg ->
                    context.actorSelection(this.idPlayer).tell(LostTurn("Lost Turn"),self)  //TODO: forse ha senso creare un messaggio di Lost da mandare all'actor che ha perso il turno
                    System.out.println("The "+ this.idPlayer + "lost turn")
                    turn()
                }
                .build()
    }

    private fun turn(){
        var selectPlayerTurn: Int
        /*if(this.turnArray.size == this.playerNumber-1){
            this.turnArray.removeAll()
        }
        var selectPlayerTurn = randomTurn()
        while(this.turnArray.contains(selectPlayerTurn)){
            selectPlayerTurn = randomTurn()
        }
        this.turnArray.plus(selectPlayerTurn)
        */
        if(this.index == this.turnArray.size-1){
            this.turnArray.sortedArray()
            this.index = 0
        }
        selectPlayerTurn = this.turnArray.get(this.index)
        this.index++
        this.idPlayer = "Player" + selectPlayerTurn
        val playerTurn = context.actorSelection(this.idPlayer)
        playerTurn.tell(ExecTurn(turnNumber), self)
        context.system.scheduler.scheduleOnce(Duration.ofMillis(10), ReceiveTimeout::class.java, self)
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
            if (this.checkSecretNumber == this.numberOfCheck) context.children.forEach { it.tell(End(this.idPlayer), self) } else context.children.forEach { it.tell(Ban(this.idPlayer), self) }
        }
    }
}