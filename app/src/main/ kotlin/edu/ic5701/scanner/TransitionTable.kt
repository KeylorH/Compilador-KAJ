package edu.ic5701.scanner

object TransitionTable {
    private val table: Map<State, Map<CharClass, State>> = mapOf(
        State.Q0 to mapOf(
            CharClass.DOT to State.IDENTIFIER_START,
            CharClass.DIGIT to State.NUMBER_INT,
            CharClass.QUOTE to State.CHAR_START,
            CharClass.AT to State.RESERVED_AT,
            CharClass.EQUAL to State.ASSIGN,
            CharClass.PLUS to State.PLUS,
            CharClass.MINUS to State.MINUS,
            CharClass.STAR to State.STAR,
            CharClass.SLASH to State.SLASH,
            CharClass.LT to State.LT,
            CharClass.GT to State.GT,
            CharClass.BANG to State.BANG,
            CharClass.AMP to State.AMP,
            CharClass.PIPE to State.PIPE,
            CharClass.LPAREN to State.LPAREN,
            CharClass.RPAREN to State.RPAREN,
            CharClass.LBRACKET to State.LBRACKET,
            CharClass.RBRACKET to State.RBRACKET,
            CharClass.COMMA to State.COMMA,
            CharClass.SEMICOLON to State.SEMICOLON,
            CharClass.WHITESPACE to State.Q0
        ),

        State.IDENTIFIER_START to mapOf(
            CharClass.LETTER to State.IDENTIFIER,
            CharClass.UNDERSCORE to State.IDENTIFIER
        ),

        State.IDENTIFIER to mapOf(
            CharClass.LETTER to State.IDENTIFIER,
            CharClass.DIGIT to State.IDENTIFIER,
            CharClass.UNDERSCORE to State.IDENTIFIER
        ),

        State.NUMBER_INT to mapOf(
            CharClass.DIGIT to State.NUMBER_INT,
            CharClass.DOT to State.NUMBER_DOT
        ),

        State.NUMBER_DOT to mapOf(
            CharClass.DIGIT to State.NUMBER_FLOAT
        ),

        State.NUMBER_FLOAT to mapOf(
            CharClass.DIGIT to State.NUMBER_FLOAT
        ),

        State.CHAR_START to stringContent(),
        State.CHAR_CONTENT to stringContent(),

        State.RESERVED_AT to mapOf(
            CharClass.LETTER to State.RESERVED_WORD
        ),

        State.RESERVED_WORD to mapOf(
            CharClass.LETTER to State.RESERVED_WORD
        ),

        State.ASSIGN to mapOf(CharClass.EQUAL to State.ASSIGN),
        State.PLUS to mapOf(CharClass.EQUAL to State.PLUS),
        State.MINUS to mapOf(CharClass.EQUAL to State.MINUS),
        State.STAR to mapOf(CharClass.EQUAL to State.STAR),
        State.SLASH to mapOf(CharClass.EQUAL to State.SLASH),
        State.LT to mapOf(CharClass.EQUAL to State.LT),
        State.GT to mapOf(CharClass.EQUAL to State.GT),
        State.BANG to mapOf(CharClass.EQUAL to State.BANG),
        State.AMP to mapOf(CharClass.AMP to State.AMP),
        State.PIPE to mapOf(CharClass.PIPE to State.PIPE)
    )

    private fun stringContent(): Map<CharClass, State> {
        return CharClass.entries.associateWith { charClass ->
            if (charClass == CharClass.QUOTE) State.CHAR_END else State.CHAR_CONTENT
        }
    }

    fun next(state: State, charClass: CharClass): State {
        return table[state]?.get(charClass) ?: State.ERROR
    }
}