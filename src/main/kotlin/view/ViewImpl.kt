package view

import akka.actor.typed.ActorRef
import message.Message
import message.StartGame
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

class ViewImpl : JFrame(), View {
    private val visualization: Visualization = Visualization()
    private val playerN = JTextField("6")
    private val secretLength = JTextField("4")
    private val humanPlayer = JCheckBox()

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
        add(JButton("Start").apply { addActionListener(object : ActionListener {
            override fun actionPerformed(e: ActionEvent) {
                actor.tell(StartGame(actor, playerN.text.toInt(), secretLength.text.toInt(), emptyList()))
                isVisible = false
                dispose()
            }
        }) }, gbc)

        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        isResizable = false
        pack()
        isVisible = true
    }

    override fun newPlayer(ID: String) { visualization.newPlayer(ID) }

    override fun newResult(attacker: String, defender: String, black: Int, white: Int) {
        visualization.newResult(attacker, defender, black, white)
    }
}