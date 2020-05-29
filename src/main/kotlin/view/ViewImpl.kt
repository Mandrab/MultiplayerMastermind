package view

import akka.actor.typed.ActorRef
import algorithm.Code
import message.CheckResult
import message.Message
import message.StartGame
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

class ViewImpl : JFrame(), View {
    private val visualization: Visualization by lazy { Visualization() }
    private val playerN = JTextField("6")
    private val secretLength = JTextField("4")
    private val humanPlayer = JCheckBox()
    private lateinit var mySecret: Code
    private lateinit var humanView: HumanView
    override lateinit var actor: ActorRef<Message>

    init {
        layout = GridBagLayout()
        val gbc = GridBagConstraints()

        gbc.fill = GridBagConstraints.BOTH
        add(JTextField("Players Number:").apply { isEditable = false }, gbc)

        gbc.gridy = 1
        add(JTextField("Secret Length:").apply { isEditable = false }, gbc)

        gbc.gridy = 2
        add(JTextField("Human Player:").apply { isEditable = false }, gbc)

        gbc.gridx = 1

        gbc.gridy = 0
        add(playerN, gbc)

        gbc.gridy = 1
        add(secretLength, gbc)

        gbc.gridy = 2
        add(humanPlayer, gbc)

        gbc.gridy = 3
        add(JButton("Start").apply { addActionListener {
            visualization.actor = actor
            visualization.secretLenght = secretLength.text.toInt()
            if (humanPlayer.isSelected){
                val jp = JOptionPane.showInputDialog(this, "Insert secret number",
                        "Secret Number", JOptionPane.QUESTION_MESSAGE)
                mySecret = Code(jp.map { Integer.parseInt(it.toString()) }.toTypedArray())
                humanView = HumanView(playerN.text.toInt(), secretLength.text.toInt(), mySecret, actor)
                humanView.isVisible = true
            }
            actor.tell(StartGame(actor, playerN.text.toInt(), secretLength.text.toInt(), humanPlayer.isSelected ,emptyList()))
            isVisible = false
            dispose()
        } }, gbc)

        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        isResizable = false
        pack()
        isVisible = true
    }

    override fun newPlayer(ID: String) { visualization.newPlayer(ID) }

    override fun newResult(attacker: String, defender: String, black: Int, white: Int) {
        visualization.newResult(attacker, defender, black, white)
    }

    override fun newBan(attacker: String){
        visualization.newBan(attacker)
    }

    override fun newLost(attacker: String, turn: Int, value: String){
        visualization.newLostTurn(attacker, turn, value)
    }

    override fun newWin(value:String){
        visualization.newWin(value)
    }

    override fun humanTurn(turn: Int){
        humanView.humanTurn(turn)
    }

    override fun lostHumanTurn(turn: Int){
        visualization.lostHumanTurn(turn)
    }

    override fun humanWannaTry(){
       humanView.tryButton.isEnabled = true
    }

    override fun humanBanned(){
        humanView.tryButton.isEnabled = false
        humanView.listButton.forEach{ it.isEnabled = false }
    }

    override fun humanBlackWhite(black: Int, white:Int){
        visualization.humanBlackWhite(black, white)
    }

    override fun humanCheck(attempt: Array<Int>, attacker: String, defender:String) {
      val result =  mySecret.guess(Code(attempt))
       actor.tell(CheckResult(actor, result.black, result.white, attacker , defender))
    }

}