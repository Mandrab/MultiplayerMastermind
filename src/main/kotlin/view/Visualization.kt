package view

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

class Visualization : JFrame() {
    private val players = mutableMapOf<String, Dialog>()
    private val playersID = JList(emptyArray<String>())

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
}