package algorithm

/**
 * Interface that represent methods that a player that aim to discover the secret code must implements.
 *
 * @author Paolo Baldini
 */
interface Challenger {

    fun makeAttempt(): Code

    fun attemptResult(response: Result)
}

interface FaultChallenger {

    fun makeAttempt(): Code

    fun attemptResult(attempt: Code, response: Result)
}