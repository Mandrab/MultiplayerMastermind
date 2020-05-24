package actor

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.*
import message.CheckResult
import message.GamePlayers
import message.Message
import message.StartGame
import view.View

class ViewActor(context: ActorContext<Message>, private val view: View) : AbstractBehavior<Message>(context) {

    fun idle() : Behavior<Message> = newReceiveBuilder()
            .onMessage(StartGame::class.java) { Services.unicast(Services.startGameServiceKey, context,
                    StartGame(context.self, it.playerCount, it.secretLength, it.players)); idle() }
            .onMessage(Services.Unicast::class.java) { res -> res.msg?.let { res.actor.tell(res.msg) }; idle() }
            .onMessage(GamePlayers::class.java) { it -> it.players.forEach { view.newPlayer(Adapter.toClassic(it)
                    .path().name()) }; idle() }.build()

    override fun createReceive(): Receive<Message> = newReceiveBuilder().onMessage(CheckResult::class.java) { apply {
        view.newResult(it.mainReceiver, it.sender.toString(), it.correctPlace, it.wrongPlace)
    } }.build()

    companion object {
        fun create(view: View): Behavior<Message> = Behaviors.setup { ViewActor(it, view).idle() }
    }
}