package view

import akka.actor.typed.ActorRef
import message.Message
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

class ViewImpl(private val actor: ActorRef<Message>) : JFrame(), View {
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

    override fun newPlayer(ID: String) {
        players[ID] = Dialog(this, ID)
    }

    override fun newResult(attacker: String, defender: String, black: Int, white: Int) {
        players[attacker]?.attempts?.add("$defender black: $black white: $white")
    }
}