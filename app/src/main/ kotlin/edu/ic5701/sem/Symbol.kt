package edu.ic5701.sem

import edu.ic5701.ast.FuncDecl
import edu.ic5701.ast.KajType

enum class SymbolCategory {
    VAR, PARAM, ARRAY, FUNC
}

data class Symbol(
    val name: String,
    var type: KajType?,
    val category: SymbolCategory,
    val function: FuncDecl? = null
)