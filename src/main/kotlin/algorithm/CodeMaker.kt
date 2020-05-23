package algorithm

/**
 * Interface that represent methods that a player that hope to hide it's secret code must implements.
 *
 * @author Paolo Baldini
 */
interface CodeMaker {

    val secret: Code

    fun verify(guess: Code): Result
}
