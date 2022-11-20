package chess

data class Row(
    val number: Int,
    val values: MutableList<String>
)

data class Player(
    val name: String,
    val mark: String
)

val markColor = mapOf(
    "W" to "white",
    "B" to "black"
)

val columnIndex = mapOf(
    "a" to 0,
    "b" to 1,
    "c" to 2,
    "d" to 3,
    "e" to 4,
    "f" to 5,
    "g" to 6,
    "h" to 7
)