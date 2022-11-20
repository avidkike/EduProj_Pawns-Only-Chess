package chess

import kotlin.math.abs

fun main() {
//    write your code here
    val players = mutableListOf<Player>()

    println("Pawns-Only Chess")

    println("First Player's name:")
    players.add(Player(readln(), "W"))   //whites
    println("Second Player's name:")
    players.add(Player(readln(), "B"))   //blacks

    val chessboard = Chessboard()

    infinite@ while (true) {
        players@ for (player in players) {
            while (true) {
                println("${player.name}'s turn:")
                val turn = readln()
                if (turn == "exit") {
                    println("Bye!")
                    return
                }
                when (chessboard.addTurn(turn, player.mark)){
                    1 -> {
                        println("Invalid Input")
                        continue
                    }
                    2 -> continue
                    else -> {
                        chessboard.printChessboard()
                        when (chessboard.result) {
                            0 -> continue@players //continue game
                            else -> {
                                println(chessboard.resultPrint)
                                println("Bye!")
                                break@infinite //finished
                            }
                        }
                    }
                }
            }
        }
    }
}

class Chessboard() {
    private var state: List<Row> = listOf(
        Row(1, MutableList(8){ " " }),
        Row(2, MutableList(8){ "W" }),
        Row(3, MutableList(8){ " " }),
        Row(4, MutableList(8){ " " }),
        Row(5, MutableList(8){ " " }),
        Row(6, MutableList(8){ " " }),
        Row(7, MutableList(8){ "B" }),
        Row(8, MutableList(8){ " " }),
    )
    private var round = 0
    private lateinit var previousTurn: Array<Int>

    init {
        printChessboard()
    }

    var result: Int = 0 //default = continue game
    var resultPrint: String = ""

/*    private fun getResult(): Int {
        return 0 // continue
    }*/

    fun addTurn(turn: String, mark: String): Int {

        if (!Regex("[a-h][1-8][a-h][1-8]").matches(turn)) {
            return 1 // check format and borders
        }
        //translate (A,n) -> (row , column)
        val srcC = arrayOf(
            turn[1].toString().toInt() - 1,
            columnIndex.getValue(turn[0].toString()).toInt())
        val trgC = arrayOf(
            turn[3].toString().toInt() - 1,
            columnIndex.getValue(turn[2].toString()).toInt())

        val maxMove = if ((srcC[0] == 1 && mark == "W") || (srcC[0] == 6 && mark == "B")) 2 else 1

        if (turn == "g2h3" ) {
            val stop = "X"
        }
        val result = checkTurn(srcC, trgC, mark, maxMove)

        if (result != 0) {
            return result
        }

        state[srcC[0]].values[srcC[1]] = " "
        state[trgC[0]].values[trgC[1]] = mark

        setResult(mark)

        return 0
    }

    private fun setResult(mark: String) {
        // row[7] contains W or row[0] contains B = Win && only B or W exist on board = Win
        if (state.any {it.number == 8 && it.values.contains("W")} ||
            state.none { it.values.any { v -> v == "B" }}) {
            resultPrint = "White Wins!"
            result = 1  //W wins
            return
        }
        if (state.any {it.number == 1 && it.values.contains("B")} ||
            state.none { it.values.any { v -> v == "W" }}) {
            resultPrint = "Black Wins!"
            result = 2  //B wins
            return
        }

        val nextMark = if (mark == "B") "W" else "B"

        var stalemate = 0

        loop@ for (row in state.filter{ it.values.contains(nextMark) }) {

            val maxMove = if ((row.number == 2 && nextMark == "W") || (row.number == 6 && nextMark == "B")) 2 else 1

            for (cell in row.values.indices) {
                if (row.values[cell] == nextMark) {

                    val srcC = arrayOf(row.number - 1, cell)

                    for (trgC in getTrgC(srcC, nextMark, maxMove).filterNot {
                        it.any { c -> c > 7 || c < 0 }
                    }) {

                        if (checkTurn(srcC, trgC, nextMark, maxMove, 0) == 0) { //if possible to make movement
                            stalemate = 1   //it is not a stalemate
                            break@loop
                        }
                    }
                }
            }
        }
        if (stalemate == 0) {
            resultPrint = "Stalemate!"
            result = 3  //B wins
        }
        // stalemate... check next player pawns movement possibility -> if any pawn has move - OK
    }

    fun getTrgC(srcC: Array<Int>, mark: String, maxMove: Int): List<Array<Int>> {
        return if (mark == "W") {
            listOf<Array<Int>>(
                arrayOf(srcC[0] + maxMove, srcC[1]),    //direct turn
                arrayOf(srcC[0] + 1, srcC[1] + 1),      //diagonal right
                arrayOf(srcC[0] + 1, srcC[1] - 1)       //diagonal left
            )
        } else {
            listOf<Array<Int>>(
                arrayOf(srcC[0] - maxMove, srcC[1]),    //direct turn
                arrayOf(srcC[0] - 1, srcC[1] + 1),      //diagonal right
                arrayOf(srcC[0] - 1, srcC[1] - 1)       //diagonal left
            )
        }
    }

    private fun checkTurn(srcC: Array<Int>, trgC: Array<Int>, mark: String, maxMove: Int, check: Int = 1): Int {

        if (state[srcC[0]].values[srcC[1]] != mark) {
            println("No ${markColor[mark]} pawn at ${columnIndex.filterValues {
                it == srcC[1]
            }.keys.joinToString("") +(srcC[0]+1).toString()}")
            return 2    //just repeat turn
        }

        if (state[trgC[0]].values[trgC[1]] == " " &&    //target cell is empty and
            abs(srcC[1] - trgC[1]) == 1 &&           //diagonal turn
            ((mark == "W" && state[trgC[0]-1].values[trgC[1]] == "B" && previousTurn.contentEquals(arrayOf(trgC[0]-1, trgC[1]))) ||
            (mark == "B" && state[trgC[0]+1].values[trgC[1]] == "W" && previousTurn.contentEquals(arrayOf(trgC[0]+1, trgC[1]))))  //there is pawn of another color in row back, same rank
        ) {
            if (check == 1) {
                if (mark == "W") {
                    state[trgC[0] - 1].values[trgC[1]] = " "
                } else {
                    state[trgC[0] + 1].values[trgC[1]] = " "
                }
            }
        } else if (state[trgC[0]].values[trgC[1]] != " " &&
                    abs(srcC[1] - trgC[1]) == 1) { // diagonal turn (1point) with capture
                //OK
        } else if (
            (mark == "W" &&
                    (trgC[0] - srcC[0] < 0 ||
                            trgC[0] - srcC[0] > maxMove)) ||
            (mark == "B" &&
                    (srcC[0] - trgC[0] < 0 ||
                            srcC[0] - trgC[0] > maxMove )) ||

            srcC[0] == trgC[0] ||   //null row shift

            (state[trgC[0]].values[trgC[1]] == " " && abs(srcC[1] - trgC[1]) > 0) ||   //diagonal without capture

            (state[trgC[0]].values[trgC[1]] != " " && (
                    abs(srcC[1] - trgC[1]) == 0 ||      //horizontal if occupied or
                        abs(srcC[1] - trgC[1]) > 1 )))  //more than 1 by diagonal
        {
            return 1
        }
        if (check == 1) previousTurn = trgC
        return 0 //success
    }

    fun printChessboard() {
        for (i in 8 downTo 1) {
            println("  +---+---+---+---+---+---+---+---+")
            printRow(state[i-1])
        }
        println("  +---+---+---+---+---+---+---+---+")
        println("    a   b   c   d   e   f   g   h")
    }
    private fun printRow(row: Row) {
        print("${row.number} |")
        for (cell in row.values) {
            print(" $cell |")
        }
        println()
    }
}