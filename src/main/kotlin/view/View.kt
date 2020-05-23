package view

interface View {

    fun newPlayer(ID: String)

    fun newResult(attacker: String, defender: String, black: Int, white: Int)
}