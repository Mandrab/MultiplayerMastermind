package view

import akka.actor.typed.ActorRef
import algorithm.Code
import message.Guess
import message.Message
import message.Try
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JTextField
import kotlin.random.Random

class HumanView(playerN: Int, secretLenght: Int, humanSecret: Code, actor:ActorRef<Message>) : JFrame(){

    val actor: ActorRef<Message> = actor
    val player : Int = playerN -1
    lateinit var humanAttempt: Array<Int>
    var myTurn: Int = 0
    var jTextFieldArray = mutableListOf<JTextField>()
    private val mySecret : Code = humanSecret
    var tryButton : JButton
    var listButton = mutableListOf<JButton>()

    init {
        layout = GridBagLayout()
        val gbc = GridBagConstraints()

        gbc.fill = GridBagConstraints.BOTH

        for(i in 0..player){
            gbc.gridy = i
            gbc.gridx=0
            val playerName = JTextField("Player$i").apply { isEditable = false }
            add(playerName, gbc)
            gbc.gridx = 1
            val textField = JTextField(" ").apply { isEditable = true; columns = 20 }
            add(textField, gbc)
            jTextFieldArray.add(textField)
            gbc.gridx = 2
            val button = JButton("Guess").apply { addActionListener {
                if (textField.text.length == secretLenght) {
                    humanAttempt = textField.text.map { Integer.parseInt(it.toString()) }.toTypedArray()
                    actor.tell(Guess(actor, myTurn, humanAttempt, "Player0", playerName.text))
                } else {
                    JOptionPane.showMessageDialog(this, "Wrong input",
                            "Wrong input", JOptionPane.ERROR_MESSAGE)
                }
            } }
            add(button, gbc)
            listButton.add(button)
        }
        gbc.gridy++
         tryButton = JButton("TryWin").apply { addActionListener {
            val x = mutableListOf(mySecret.code.toTypedArray())
            x.addAll(jTextFieldArray.map { it.text.map { Integer.parseInt(it.toString()) } .toTypedArray()})
            if(x.any { it.size!=secretLenght } )
                JOptionPane.showMessageDialog(this, "Wrong input", "Wrong input", JOptionPane.ERROR_MESSAGE)
            else actor.tell(Try(actor, myTurn, x.toTypedArray()))
        } }
        add(tryButton, gbc)
        tryButton.isEnabled = false
        pack()
    }


    fun humanTurn(turn:Int){
        myTurn = turn
         JOptionPane.showMessageDialog(this, "It's your turn$turn",
                "Turn", JOptionPane.INFORMATION_MESSAGE)
    }

}