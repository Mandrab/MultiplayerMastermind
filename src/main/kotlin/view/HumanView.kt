package view

import akka.actor.typed.ActorRef
import message.Guess
import message.Message
import message.Try
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

class HumanView(
        private val playersCount: Int,
        private val secretLength: Int,
        private val mySecret: Array<Int>
) : JFrame(){
    private val attemptsFields = mutableListOf<JTextField>()
    private val listButton = mutableListOf<JButton>()
    private val tryButton = JButton("TryWin")

    lateinit var actor: ActorRef<Message>

    var turn: Int = 0

    var status: JTextField = JTextField().apply { columns = 25 }

    init {
        layout = GridBagLayout()

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH

        (1 until playersCount).onEach { i ->
            val playerName = JTextField("Player$i").apply { isEditable = false }
            add(playerName, gbc.apply { gridx = 0; gridy = i })

            val codeField = JTextField().apply { columns = 20 }
            attemptsFields.add(codeField)
            add(codeField, gbc.apply { gridx = 1 })

            val button = JButton("Guess").apply {
                isEnabled = false
                addActionListener {
                if (codeField.text.length == secretLength) {
                    val humanAttempt = codeField.text.map { Integer.parseInt(it.toString()) }.toTypedArray()
                    actor.tell(Guess(actor, turn, humanAttempt, "Player0", playerName.text))
                } else JOptionPane.showMessageDialog(this, "Wrong input", "Wrong input",
                        JOptionPane.ERROR_MESSAGE)
            } }
            listButton.add(button)
            add(button, gbc.apply { gridx = 2 })
        }

        tryButton.addActionListener {
            val codes = mutableListOf(mySecret)
            codes.addAll(attemptsFields.map { f -> f.text.map { Integer.parseInt(it.toString()) }.toTypedArray() })
            if (codes.all { it.size == secretLength }) actor.tell(Try(actor, turn, codes.toTypedArray()))
            else JOptionPane.showMessageDialog(this, "Wrong input", "Wrong input", JOptionPane.ERROR_MESSAGE)
        }
        tryButton.isEnabled = false
        add(tryButton, gbc.apply { ++gridy })

        gbc.apply { gridx = 0; gridwidth = 2 }
        add(status, gbc.apply { gridx = 0 })

        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        isResizable = false
        pack()
        isVisible = true
    }

    fun wannaTry() {
        listButton.forEach { it.isEnabled = false }
        tryButton.isEnabled = true
    }

    fun ban() {
        tryButton.isEnabled = false
        listButton.forEach { it.isEnabled = false }

        status.text = "You have lost the game."
        status.background = Color.RED
    }

    fun execTurn(turn: Int) {
        this.turn = turn
        status.text = "It's your turn $turn"
        status.background = Color.GREEN
        listButton.forEach { it.isEnabled = true }
        tryButton.isEnabled = true
    }

    fun lostTurn(turn: Int) {
        status.text = "Lost turn $turn"
        status.background = Color.RED
        listButton.forEach { it.isEnabled = false }
        tryButton.isEnabled = false
    }

    fun newResult(defender: String, black: Int, white: Int) {
        status.text = "Last attempt against $defender got $black black and $white white."
        status.background = Color.CYAN
    }

    fun stop() {
        status.text = "Game has been stopped."
        status.background = Color.RED
        listButton.forEach { it.isEnabled = false }
        tryButton.isEnabled = false
    }

    fun endGame(win: Boolean) {
        if (win) {
            status.text = "You have win the game!"
            status.background = Color.GREEN
            listButton.forEach { it.isEnabled = false }
            tryButton.isEnabled = false
        }
        else {
            status.text = "Game ended. You loose"
            status.background = Color.RED
            listButton.forEach { it.isEnabled = false }
            tryButton.isEnabled = false
        }
    }
}