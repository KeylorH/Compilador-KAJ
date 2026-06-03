package edu.ic5701.parser

data class SyntaxError(
    val message: String,
    val line: Int,
    val column: Int
)