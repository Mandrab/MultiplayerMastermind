package actor

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import message.CheckResult
import message.Message
import message.StartGame
import view.View

class ViewActor(context: ActorContext<Message>, private val view: View) : AbstractBehavior<Message>(context) {
    override fun createReceive(): Receive<Message> = newReceiveBuilder().onMessage(StartGame::class.java) { apply {
        Services.broadcastList(Services.startGameServiceKey, context, it)
    } }.onMessage(Services.Broadcast::class.java) { broadcast -> apply {
        broadcast.msg?.let { broadcast.actors.foreach { it.tell(broadcast.msg) } }
    } }.onMessage(CheckResult::class.java) { apply {
        view.newResult(it.mainReceiver, it.sender.toString(), it.correctPlace, it.wrongPlace)
    } }.build()

    companion object {
        fun create(view: View): Behavior<Message> = Behaviors.setup { ViewActor(it, view) }
    }
}