package actor

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.*
import akka.actor.typed.receptionist.Receptionist
import message.*
import view.GameView

/*
  This is a ViewActor class.
  This class when received message call a view method to update information.

  @author Baldini Paolo, Battistini Ylenia
 */
class ViewActor(context: ActorContext<Message>, private val view: GameView) : AbstractBehavior<Message>(context) {

    /*
     When viewActor received StartGame message sends it message to those who have registered for the service.
     If receive GamePlayers, foreach players call a newPlayer method for update a view information.
     */
    private fun start() : Behavior<Message> = newReceiveBuilder().onMessage(StartGame::class.java) {
                Services.unicast(Services.Service.START_GAME.key, context, it); start()
            }
            .onMessage(Services.Unicast::class.java) {
                it.actor?.apply { it.msg?.apply { it.actor.tell(it.msg) } }; start()
            }
            .onMessage(GamePlayers::class.java) { apply {
                it.players.forEach { view.newPlayer(Adapter.toClassic(it).path().name()) }
            } }.build()

    /*
     When viewActor received message he call a view method to update a view information.
     */
    override fun createReceive(): Receive<Message> = newReceiveBuilder()
            .onMessage(CheckResult::class.java) { apply {
                view.newResult(Adapter.toClassic(it.sender).path().name(), it.attackerID, it.black, it.white)
            } }
            .onMessage(Ban::class.java) { apply { view.newBan(it.playerID) } }
            .onMessage(LostTurn::class.java) { apply { view.newLostTurn(it.attackerID, it.turn) } }
            .onMessage(End::class.java) { apply {
                if (it.winnerID.isEmpty()) view.newWin("No one won the game.")
                else view.newWin("Game ended. The winner is: ${it.winnerID}.")
            } }
            .onMessage(StopGame::class.java) { apply { Services.unicast(Services.Service.STOP_GAME.key, context, it) } }
            .onMessage(Services.Unicast::class.java) { res -> apply { res.msg?.let { res.actor?.tell(res.msg) } } }
            .build()

    /*
     ViewActor registers to be able to receive attempts and then it start.
     */
    companion object {
        fun create(view: GameView): Behavior<Message> = Behaviors.setup {
            it.system.receptionist().tell(Receptionist.register(Services.Service.OBSERVE_RESULT.key, it.self))
            ViewActor(it, view).start()
        }
    }
}