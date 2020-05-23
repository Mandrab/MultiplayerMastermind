package message

data class Check(val attempt: Array<Int>){
    //TODO: CHIEDERE non è più facile se Check prende un Int e l'arbitro smista i messaggi ? tanto se ho un check normale avrò attempt a posizione 0 se invece
    //ho un check per la vincita avrò in posizione i l'attempt per il player i
    override fun equals(other: Any?): Boolean {
        /*if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Guess

        if (!attempt.contentEquals(other.attempt)) return false

        return true*/ TODO()
    }

    override fun hashCode(): Int {
        return attempt.contentHashCode()
    }
}