package view

import akka.actor.typed.ActorRef
import message.Message
import message.StartGame
import message.StopGame
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*
import kotlin.system.exitProcess
/*
This class contains all the information for each player (attempts, lost, banned player)

@author Baldini Paolo, Battistini Ylenia
 */
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
                isEnabled = false
                exitProcess(0)
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

    /*
    This method add a new player in a player list.

     @param ID, playerID.
     */
    fun newPlayer(ID: String) {
        players[ID] = Dialog(this, ID)
        playersID.setListData(players.keys.toTypedArray())
    }

    /*
    This method add a new result

    @param attacker, player who send Guess
    @param defender, player i want to guess
    @param black, digits in right place
    @param white, digits in wrong place
     */
    fun newResult(attacker: String, defender: String, black: Int, white: Int) {
        players[attacker]?.let {
            it.attempts.add("$defender    |    black: $black    white: $white    ")
            it.attemptsList.setListData(it.attempts.toTypedArray())
            it.pack()
        }
    }

    /*
    This method add a new ban in a player list.

     @param attacker, playerID.
     */
    fun newBan(attacker: String) {
        players[attacker]?.let {
            it.attempts.add("Game lost")
            it.attemptsList.setListData(it.attempts.toTypedArray())
            it.pack()
        }
    }

    /*
    This method add a lost turn string in a player list.

     @param attacker, playerID.
     @param turn, represent turn number
     */
    fun newLostTurn(attacker: String, turn: Int){
        players[attacker]?.let {
            it.attempts.add("Turn $turn lost")
            it.attemptsList.setListData(it.attempts.toTypedArray())
            it.pack()
        }
    }

    /*
    This method create a JOptionPane for winner.

     @param value, player who won.
     */
    fun newWin(value:String) {
        JOptionPane.showMessageDialog(this, value,
                "End Game", JOptionPane.INFORMATION_MESSAGE)
    }
}