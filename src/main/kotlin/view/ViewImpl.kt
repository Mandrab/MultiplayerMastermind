package view

import controller.Controller
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

/**
 * This class is the main view where the user can choose the number of players,
 * the length of the secret number and if there is also a human player.
 *
 * @param controller, Controller
 *
 * @author Baldini Paolo, Battistini Ylenia
 */
class ViewImpl(controller: Controller) : JFrame(), View {
    private val playerCountField = JTextField("6")
    private val secretLengthField = JTextField("4")
    private val humanPlayerBox = JCheckBox()

    private val playerCount: Int
        get() = playerCountField.text.toInt()
    private val secretLength: Int
        get() = secretLengthField.text.toInt()
    private var mySecret = Array(secretLength) { 0 }

    override val gameView: GameView by lazy { GameView() }
    override val humanView: HumanView by lazy { HumanView(playerCount, secretLength, mySecret) }

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
        add(playerCountField, gbc)

        gbc.gridy = 1
        add(secretLengthField, gbc)

        gbc.gridy = 2
        add(humanPlayerBox, gbc)

        gbc.gridy = 3
        add(JButton("Start").apply { addActionListener {
            if (humanPlayerBox.isSelected) {
                var validSecret = false

                do {
                    val secretText = JOptionPane.showInputDialog(this, "Insert secret number",
                            "Secret Number", JOptionPane.QUESTION_MESSAGE)

                    secretText?.let {
                        mySecret = secretText.mapNotNull { "$it".toIntOrNull() }.toTypedArray()
                        validSecret = secretText.all { it.isDigit() } && mySecret.size == secretLength
                    }
                } while (!validSecret && secretText != null)

                if (validSecret) {
                    val humanPlayer = controller.humanPlayer(mySecret)
                    humanView.actor = humanPlayer
                    gameView.start(playerCount, secretLength, humanPlayer)
                } else gameView.start(playerCount, secretLength, null)
            } else gameView.start(playerCount, secretLength, null)

            dispose()
        } }, gbc)

        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        isResizable = false
        pack()
        isVisible = true
    }
}