package view

import akka.actor.typed.ActorRef
import message.Message
import message.StartGame
import message.StopGame
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

class GameView : JFrame() {
    private val players = mutableMapOf<String, Dialog>()
    private val playersID = JList(emptyArray<String>())

    lateinit var actor: ActorRef<Message>

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
    }

    fun start(playerCount: Int, secretLength: Int, humanActor: ActorRef<Message>?) {
        actor.tell(StartGame(actor, playerCount, secretLength, humanActor, emptyList()))
        isVisible = true
    }

    private class Dialog(frame: JFrame, playerID: String) : JDialog(frame, playerID) {
        val attempts = mutableListOf<String>()
        val attemptsList = JList<String>()

        init {
            layout = GridBagLayout()
            val gbc = GridBagConstraints()

            gbc.fill = GridBagConstraints.BOTH
            add(JTextField("Attackers").apply { isEditable = false }, gbc)

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

    fun newLostTurn(attacker: String, turn: Int){
        players[attacker]?.let {
            it.attempts.add("Turn $turn lost")
            it.attemptsList.setListData(it.attempts.toTypedArray())
            it.pack()
        }
    }

    fun newWin(value:String) {
        JOptionPane.showMessageDialog(this, value,
                "End Game", JOptionPane.INFORMATION_MESSAGE)
    }
}