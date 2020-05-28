package actor

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.*
import akka.actor.typed.receptionist.Receptionist
import message.*
import view.View

class ViewActor(context: ActorContext<Message>, private val view: View) : AbstractBehavior<Message>(context) {

    fun idle() : Behavior<Message> = newReceiveBuilder()
            .onMessage(StartGame::class.java) { Services.unicast(Services.Service.START_GAME.key, context,
                    StartGame(context.self, it.playerCount, it.secretLength, it.players)); idle() }
            .onMessage(Services.Unicast::class.java) { res -> res.msg?.let { res.actor.tell(res.msg) }; idle() }
            .onMessage(GamePlayers::class.java) { apply { it.players.forEach { view.newPlayer(Adapter.toClassic(it)
                    .path().name()) } } }.build()

    override fun createReceive(): Receive<Message> = newReceiveBuilder()
            .onMessage(Ban::class.java) { apply {
                view.newBan(it.playerID)
            }}
            .onMessage(LostTurn::class.java){apply {
                view.newLost(it.attackerID, it.turn, it.lostTurn)
            }}
            .onMessage(End::class.java) {apply {
                if(it.winnerID.isEmpty()){
                    view.newWin("No one won the game")
                } else {
                    view.newWin("Game ended. The winner is: "+ it.winnerID)
                }
            }}
            .onMessage(CheckResult::class.java) { apply {
                view.newResult(Adapter.toClassic(it.sender).path().name(), it.attackerID, it.black, it.white)
            }}
            .onMessage(StopGame::class.java) { apply { Services.unicast(Services.Service.STOP_GAME.key, context, it) } }
            .onMessage(Services.Unicast::class.java) { res -> apply { res.msg?.let { res.actor.tell(res.msg) } } }
            .build()

    companion object {
        fun create(view: View): Behavior<Message> = Behaviors.setup {
            it.system.receptionist().tell(Receptionist.register(Services.Service.OBSERVE_RESULT.key, it.self))
            ViewActor(it, view).idle()
        }
    }
}