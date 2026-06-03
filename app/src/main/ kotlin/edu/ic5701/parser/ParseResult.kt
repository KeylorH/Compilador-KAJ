package edu.ic5701.parser

import edu.ic5701.ast.Program

data class ParseResult(
    val program: Program?,
    val errors: List<SyntaxError>
) {
    val isSuccess: Boolean
        get() = errors.isEmpty()
}