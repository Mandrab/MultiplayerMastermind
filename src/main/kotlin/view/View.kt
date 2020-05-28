package view

import akka.actor.typed.ActorRef
import message.Message

interface View {

    var actor: ActorRef<Message>

    fun newPlayer(ID: String)

    fun newResult(attacker: String, defender: String, black: Int, white: Int)

    fun newBan(attacker: String)

    fun newLost(attacker: String, turn:Int, value:String)

    fun newWin(value:String)
}