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
        add(JTextField("Players"), gbc)

        gbc.gridy = 1
        playersID.addListSelectionListener {
            JOptionPane.showMessageDialog(this, "")
        }
        add(JScrollPane(playersID), gbc)

        isResizable = false
        pack()
        isVisible = true
    }

    private class Dialog(frame: JFrame, playerID: String) : JDialog(frame, playerID) {
        val attempts = mutableListOf<String>()

        init {
            layout = GridBagLayout()
            val gbc = GridBagConstraints()

            gbc.fill = GridBagConstraints.BOTH
            add(JTextField("Attempts"), gbc)

            gbc.gridy = 1
            add(JScrollPane(JList(attempts.toTypedArray())), gbc)

            isResizable = false
            pack()
        }
    }

    fun newPlayer(ID: String) {
        players[ID] = Dialog(this, ID)
    }

    fun newResult(attacker: String, defender: String, black: Int, white: Int) {
        players[attacker]?.attempts?.add("$defender black: $black white: $white")
    }
}