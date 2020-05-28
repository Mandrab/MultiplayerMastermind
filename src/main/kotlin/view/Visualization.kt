package view

import akka.actor.typed.ActorRef
import algorithm.Code
import algorithm.CodeMaker
import message.CheckResult
import message.Guess
import message.Message
import message.StopGame
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*
import kotlin.random.Random

class Visualization : JFrame() , CodeMaker{
    private val players = mutableMapOf<String, Dialog>()
    private val playersID = JList(emptyArray<String>())

    lateinit var actor: ActorRef<Message>
    lateinit var humanAttempt: Array<Int>
    var secretLenght: Int = 0

    override val secret by lazy { Code() }
    override fun verify(guess: Code) = secret.guess(guess)

    init {
        layout = GridBagLayout()
        val gbc = GridBagConstraints()

        gbc.fill = GridBagConstraints.BOTH
        add(JTextField("Players").apply { isEditable = false }, gbc)

        gbc.gridy = 1
        playersID.selectionMode = DefaultListSelectionModel.SINGLE_SELECTION
        playersID.addListSelectionListener {
            players[playersID.selectedValue]?.isVisible = true
        }
        add(JScrollPane(playersID), gbc)

        gbc.gridy = 3
        add(JButton("End Game").apply {
            addActionListener {
                actor.tell(StopGame(actor))
                this.isEnabled = false
            }
        }, gbc)

        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        isResizable = false
        pack()
        isVisible = true
    }

    private class Dialog(frame: JFrame, playerID: String) : JDialog(frame, playerID) {
        val attempts = mutableListOf<String>()
        val attemptsList = JList<String>()

        init {
            layout = GridBagLayout()
            val gbc = GridBagConstraints()

            gbc.fill = GridBagConstraints.BOTH
            add(JTextField("Attempts").apply { isEditable = false }, gbc)

            gbc.gridy = 1
            attemptsList.selectionMode = DefaultListSelectionModel.SINGLE_SELECTION
            add(JScrollPane(attemptsList), gbc)

            defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
            isResizable = false
            pack()
        }
    }

    fun newPlayer(ID: String) {
        players[ID] = Dialog(this, ID)
        playersID.setListData(players.keys.toTypedArray())
    }

    fun newResult(attacker: String, defender: String, black: Int, white: Int) {
        players[attacker]?.let {
            it.attempts.add("$defender    |    black: $black    white: $white    ")
            it.attemptsList.setListData(it.attempts.toTypedArray())
            it.pack()
        }
    }

    fun newBan(attacker: String) {
        players[attacker]?.let {
            it.attempts.add("Banned!!! ")
            it.attemptsList.setListData(it.attempts.toTypedArray())
            it.pack()
        }
    }

    fun newLostTurn(attacker: String, turn: Int, value: String){
        players[attacker]?.let {
            it.attempts.add("$value $turn.")
            it.attemptsList.setListData(it.attempts.toTypedArray())
            it.pack()
        }
    }

    fun newWin(value:String){
        JOptionPane.showMessageDialog(this, value,
                "End Game", JOptionPane.INFORMATION_MESSAGE)
    }

    fun humanStartGame(){
            //TODO. l'umano deve scegliere un numero ma come lo salvo ? ? come fai dopo a fare la check?
    }

    fun humanTurn(turn: Int){
        val jp = JOptionPane.showInputDialog(this, "It's your turn$turn",
                "Turn", JOptionPane.QUESTION_MESSAGE)
        var i = 0
        jp.forEach { humanAttempt[i++] = it.toInt() }
        if ( humanAttempt.size!= secretLenght) {//TODO: valori != da interi
            val rand = Random.nextInt(1, players.size)
            actor.tell(Guess(actor, turn, humanAttempt, "Player0", "Player$rand"))
        }else JOptionPane.showMessageDialog(this, "Wrong attempt",
                "End Game", JOptionPane.ERROR_MESSAGE)
    }

    fun humanCheck(attempt: Array<Int>, sender: ActorRef<Message>, defender: String){
        //TODO: strategia per verificare il numero
        //actor.tell(CheckResult(actor, result.black, result.white, "Player0", defender)
    }

    fun lostHumanTurn(turn: Int){
        JOptionPane.showMessageDialog(this, "Timeout! Lost turn$turn",
                "Lost Turn", JOptionPane.INFORMATION_MESSAGE)
    }

    fun humanWannaTry(){
        JOptionPane.showInputDialog(this, "Try Win?",
                "Wanna Try", JOptionPane.QUESTION_MESSAGE)
        //TODO: mando ad actorView try si o no con tutti i tentativi
        //TODO-> PROBLEMA!!!!!!
    }


    fun humanBanned(){
        JOptionPane.showMessageDialog(this, "Banned!!!",
                "Banned", JOptionPane.INFORMATION_MESSAGE)
    }

    fun humanBlackWhite(black: Int, white:Int){
        JOptionPane.showMessageDialog(this, "Got $black black and $white white.",
                "BlackWhite", JOptionPane.INFORMATION_MESSAGE)
    }
}