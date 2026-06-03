package edu.ic5701.scanner

data class Token(
    val type: TokenType,
    val lexeme: String,
    val line: Int,
    val column: Int,
    val message: String? = null
)