package algorithm

/**
 * Implementation of code-maker interface.
 *
 * @author Paolo Baldini
 */
class CodeMakerImpl : CodeMaker {

    override val secret = Code()

    override fun verify(guess: Code) = secret.guess(guess)
}