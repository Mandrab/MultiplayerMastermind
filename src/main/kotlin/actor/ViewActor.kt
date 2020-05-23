package actor

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import message.CheckResult
import message.Message
import view.View

class ViewActor(context: ActorContext<Message>, private val view: View) : AbstractBehavior<Message>(context) {
    override fun createReceive(): Receive<Message> = newReceiveBuilder().onMessage(CheckResult::class.java) {
        view.newResult(it.mainReceiver.toString(), it.sender.toString(), it.correctPlace, it.wrongPlace)
        this@ViewActor
    }.build()

    companion object {
        fun create(view: View): Behavior<Message> = Behaviors.setup { ViewActor(it, view) }
    }
}