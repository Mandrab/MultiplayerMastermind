package actor

import akka.actor.AbstractActor
import akka.actor.ReceiveTimeout
import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.Adapter
import akka.actor.typed.receptionist.Receptionist
import message.*
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


    // TODO
    lateinit var playerX : ActorRef<Message>

    override fun preStart() {
        super.preStart()
        Adapter.toTyped(context.system).receptionist().tell(Receptionist
                .register(Services.startGameServiceKey, Adapter.toTyped(self)))
    }

    private fun start(msg: StartGame) : List<ActorRef<Message>> {
        this.secretValueLength = msg.secretLength
        this.playerNumber = msg.playerCount
        this.turnArray = (0 until playerNumber).toList().toTypedArray()

        val players = (0 until playerNumber).map {
            Adapter.spawn(context, PlayerActor.create("Player$it"), "Player$it") }
        //TODO ? Adapter.watch(context, player)
        return players.onEach { it.tell(StartGame(typedSelf, playerNumber, secretValueLength, players)) }
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(StartGame::class.java) { msg ->
                    start(msg).let { msg.sender.tell(GamePlayers(typedSelf, it)) }
                    turn()
                }
                .match(Guess::class.java){ msg ->
                    if (msg.playerID.equals(this.idPlayer)) {
                        val playerID = context.actorSelection(msg.playerID);
                        playerID.tell(Check(typedSelf, msg.attempt), self)
                        this.idPlayer = msg.playerID
                        playerX = msg.sender
                    } else context.children.forEach{it.tell(NotCheck(typedSelf, msg.playerID, msg.turn),self)}
                }
                .match(CheckResult::class.java) { msg ->
                    // TODO check
                    if (!this.tryWin) consolePrint(msg.correctPlace, msg.wrongPlace) else checkWin(msg.correctPlace, msg.wrongPlace)
                    playerX.tell(WannaTry(typedSelf, this.turnNumber))
                }
                .match(Try::class.java){ msg ->
                    //TODO: verificare cosa torna Paul
                    var children = context.children
                    var i = -1
                    children.forEach{i++; it.tell(Check(typedSelf, msg.attempt!![i]), self)}
                    this.tryWin = true
                }
                .match(ReceiveTimeout::class.java) {msg ->
                    context.actorSelection(this.idPlayer).tell(LostTurn(typedSelf, "Lost Turn"),self)  //TODO: forse ha senso creare un messaggio di Lost da mandare all'actor che ha perso il turno
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
        if (this.index == this.turnArray.size-1) {
            this.turnArray.sortedArray()
            this.index = 0
        }
        selectPlayerTurn = this.turnArray.get(this.index)
        this.index++
        this.idPlayer = "Player" + selectPlayerTurn
        val playerTurn = context.actorSelection(this.idPlayer)
        playerTurn.tell(ExecTurn(typedSelf, turnNumber), self)
        context.system.scheduler.scheduleOnce(Duration.ofMillis(10)!!, {
            TODO()
        }, context.system.dispatcher)
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
            if (this.checkSecretNumber == this.numberOfCheck) context.children.forEach { it.tell(End(typedSelf, this.idPlayer), self) } else context.children.forEach { it.tell(Ban(typedSelf, this.idPlayer), self) }
        }
    }
}