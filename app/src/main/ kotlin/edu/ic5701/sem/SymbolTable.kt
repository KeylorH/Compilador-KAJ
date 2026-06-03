package edu.ic5701.sem

class SymbolTable {
    private val scopes = mutableListOf<MutableMap<String, Symbol>>()

    init {
        newScope()
    }

    fun newScope() {
        scopes.add(mutableMapOf())
    }

    fun popScope() {
        if (scopes.isNotEmpty()) scopes.removeAt(scopes.lastIndex)
    }

    fun define(symbol: Symbol): Boolean {
        val current = scopes.last()
        if (current.containsKey(symbol.name)) return false
        current[symbol.name] = symbol
        return true
    }

    fun resolve(name: String): Symbol? {
        for (scope in scopes.asReversed()) {
            if (scope.containsKey(name)) return scope[name]
        }
        return null
    }
}