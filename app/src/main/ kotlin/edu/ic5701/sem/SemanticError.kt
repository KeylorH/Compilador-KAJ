package edu.ic5701.sem

data class SemanticError(
    val message: String,
    val line: Int,
    val column: Int
)