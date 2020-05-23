package algorithm

/**
 * Implementation of mode-maker interface.
 *
 * @author Paolo Baldini
 */
class CodeMakerImpl : CodeMaker {

    override val secret = Code()

    override fun verify(guess: Code) = secret.guess(guess)
}