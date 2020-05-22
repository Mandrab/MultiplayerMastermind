package algorithm

/**
 * Implementation of mode-maker interface.
 *
 * @author Paolo Baldini
 */
class CodeMakerImpl : CodeMaker {

    override val answer = Code()

    override fun verify(guess: Code) = answer.guess(guess).apply { println(answer) }
}