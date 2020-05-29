package view

import akka.actor.typed.ActorRef
import algorithm.Code
import message.Guess
import message.Message
import message.Try
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JTextField

class HumanView(playerN: Int, secretLenght: Int, humanSecret: Code, actor:ActorRef<Message>) : JFrame(){

    val actor: ActorRef<Message> = actor
    val player : Int = playerN -1
    lateinit var humanAttempt: Array<Int>
    var myTurn: Int = 0
    var jTextFieldArray = mutableListOf<JTextField>()
    private val mySecret : Code = humanSecret
    var tryButton : JButton
    var listButton = mutableListOf<JButton>()
    var status: JTextField = JTextField().apply { columns = 25 }

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
            val textField = JTextField("").apply { isEditable = true; columns = 20 }
            add(textField, gbc)
            jTextFieldArray.add(textField)
            gbc.gridx = 2
            val button = JButton("Guess").apply { addActionListener {
                println("TESTO->" + textField.text.length + "SECRET" + secretLenght)
                if (textField.text.length == secretLenght) {
                    humanAttempt = textField.text.map { Integer.parseInt(it.toString()) }.toTypedArray()
                    actor.tell(Guess(actor, myTurn, humanAttempt, "Player0", playerName.text))
                } else {
                    JOptionPane.showMessageDialog(this, "Wrong input",
                            "Wrong input", JOptionPane.ERROR_MESSAGE)
                }
            } }
            add(button, gbc)
            button.isEnabled = false
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
        gbc.gridx = 0
        add(status, gbc)
        pack()
    }


    fun humanTurn(turn:Int){
        myTurn = turn
        status.text = "It's your turn $turn"
        status.background = Color.GREEN
        listButton.forEach { it.isEnabled = true }
    }

    fun lostHumanTurn(turn:Int){
        status.text = "Lost turn $turn"
        status.background = Color.RED
    }

    fun humanBlackWhite(black:Int, white:Int){
        status.text = "Got $black black and $white white."
        status.background = Color.BLUE
    }

    fun humanBanned(){
        status.text ="You have been terminated."
        status.background = Color.RED
    }
}