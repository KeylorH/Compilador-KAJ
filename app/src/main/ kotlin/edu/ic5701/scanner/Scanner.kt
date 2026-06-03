package edu.ic5701.scanner

class Scanner(private val source: String) {
    private var position = 0
    private var line = 1
    private var column = 1

    fun scanTokens(): List<Token> {
        val tokens = mutableListOf<Token>()

        while (true) {
            val token = nextToken()
            tokens.add(token)

            if (token.type == TokenType.EOF) break
        }

        return tokens
    }

    private fun nextToken(): Token {
        var state = State.Q0
        val lexeme = StringBuilder()

        var startLine = line
        var startColumn = column

        var lastAcceptedState: State? = null
        var lastAcceptedLexeme = ""
        var lastAcceptedPosition = position
        var lastAcceptedLine = line
        var lastAcceptedColumn = column

        while (!isAtEnd()) {
            val c = peek()
            val charClass = classify(c)
            val nextState = TransitionTable.next(state, charClass)

            if (state == State.Q0 && charClass == CharClass.WHITESPACE) {
                advance()
                startLine = line
                startColumn = column
                continue
            }

            if (nextState == State.ERROR) {
                if (state == State.NUMBER_DOT) {
                    return errorToken(
                        lexeme.toString(),
                        startLine,
                        startColumn,
                        "Número decimal inválido '${lexeme}'. Debe haber dígitos después del punto."
                    )
                }

                if (state == State.IDENTIFIER_START) {
                    return errorToken(
                        lexeme.toString(),
                        startLine,
                        startColumn,
                        "Identificador inválido '${lexeme}'. Después de '.' debe venir una letra o '_'."
                    )
                }

                if (state == State.CHAR_START || state == State.CHAR_CONTENT) {
                    return errorToken(
                        lexeme.toString(),
                        startLine,
                        startColumn,
                        "Hilera inválida '${lexeme}'. Falta la comilla doble de cierre."
                    )
                }

                if (lastAcceptedState != null) {
                    position = lastAcceptedPosition
                    line = lastAcceptedLine
                    column = lastAcceptedColumn

                    return buildToken(
                        lastAcceptedState,
                        lastAcceptedLexeme,
                        startLine,
                        startColumn
                    )
                }

                if (lexeme.isNotEmpty()) {
                    return errorToken(
                        lexeme.toString(),
                        startLine,
                        startColumn,
                        messageForError(lexeme.toString())
                    )
                }

                advance()

                return errorToken(
                    c.toString(),
                    startLine,
                    startColumn,
                    "Símbolo inválido '$c'. Este símbolo no pertenece al lenguaje KAJ."
                )
            }

            lexeme.append(advance())
            state = nextState

            if (isAcceptingState(state, lexeme.toString())) {
                lastAcceptedState = state
                lastAcceptedLexeme = lexeme.toString()
                lastAcceptedPosition = position
                lastAcceptedLine = line
                lastAcceptedColumn = column
            }
        }

        if (lastAcceptedState != null) {
            return buildToken(
                lastAcceptedState,
                lastAcceptedLexeme,
                startLine,
                startColumn
            )
        }

        if (lexeme.isNotEmpty()) {
            return errorToken(
                lexeme.toString(),
                startLine,
                startColumn,
                messageForError(lexeme.toString())
            )
        }

        return Token(TokenType.EOF, "", line, column)
    }

    private fun classify(c: Char): CharClass {
        return when {
            c in 'a'..'z' || c in 'A'..'Z' -> CharClass.LETTER
            c in '0'..'9' -> CharClass.DIGIT
            c == '_' -> CharClass.UNDERSCORE
            c == '@' -> CharClass.AT
            c == '.' -> CharClass.DOT
            c == '"' -> CharClass.QUOTE
            c == '=' -> CharClass.EQUAL
            c == '+' -> CharClass.PLUS
            c == '-' -> CharClass.MINUS
            c == '*' -> CharClass.STAR
            c == '/' -> CharClass.SLASH
            c == '<' -> CharClass.LT
            c == '>' -> CharClass.GT
            c == '!' -> CharClass.BANG
            c == '&' -> CharClass.AMP
            c == '|' -> CharClass.PIPE
            c == '(' -> CharClass.LPAREN
            c == ')' -> CharClass.RPAREN
            c == '[' -> CharClass.LBRACKET
            c == ']' -> CharClass.RBRACKET
            c == ',' -> CharClass.COMMA
            c == ';' -> CharClass.SEMICOLON
            c.isWhitespace() -> CharClass.WHITESPACE
            else -> CharClass.OTHER
        }
    }

    private fun isAcceptingState(state: State, lexeme: String): Boolean {
        return when (state) {
            State.IDENTIFIER,
            State.NUMBER_INT,
            State.NUMBER_FLOAT,
            State.CHAR_END,
            State.RESERVED_WORD,
            State.ASSIGN,
            State.PLUS,
            State.MINUS,
            State.STAR,
            State.SLASH,
            State.LT,
            State.GT,
            State.BANG,
            State.LPAREN,
            State.RPAREN,
            State.LBRACKET,
            State.RBRACKET,
            State.COMMA,
            State.SEMICOLON -> true

            State.AMP -> lexeme == "&&"
            State.PIPE -> lexeme == "||"

            else -> false
        }
    }

    private fun buildToken(
        state: State,
        lexeme: String,
        line: Int,
        column: Int
    ): Token {
        val type = when (state) {
            State.IDENTIFIER -> TokenType.IDENTIFIER

            State.NUMBER_INT,
            State.NUMBER_FLOAT -> TokenType.NUMBER

            State.CHAR_END -> TokenType.STRING

            State.RESERVED_WORD -> reservedTokenType(lexeme)

            State.ASSIGN -> if (lexeme == "==") TokenType.EQ else TokenType.ASSIGN
            State.PLUS -> if (lexeme == "+=") TokenType.PLUS_ASSIGN else TokenType.PLUS
            State.MINUS -> if (lexeme == "-=") TokenType.MINUS_ASSIGN else TokenType.MINUS
            State.STAR -> if (lexeme == "*=") TokenType.MUL_ASSIGN else TokenType.STAR
            State.SLASH -> if (lexeme == "/=") TokenType.DIV_ASSIGN else TokenType.SLASH
            State.LT -> if (lexeme == "<=") TokenType.LE else TokenType.LT
            State.GT -> if (lexeme == ">=") TokenType.GE else TokenType.GT
            State.BANG -> if (lexeme == "!=") TokenType.NE else TokenType.NOT
            State.AMP -> TokenType.AND
            State.PIPE -> TokenType.OR

            State.LPAREN -> TokenType.LPAREN
            State.RPAREN -> TokenType.RPAREN
            State.LBRACKET -> TokenType.LBRACKET
            State.RBRACKET -> TokenType.RBRACKET
            State.COMMA -> TokenType.COMMA
            State.SEMICOLON -> TokenType.SEMICOLON

            else -> TokenType.ERROR
        }

        if (type == TokenType.ERROR) {
            return errorToken(
                lexeme,
                line,
                column,
                "Palabra reservada inválida '$lexeme'. No pertenece al lenguaje KAJ."
            )
        }

        return Token(type, lexeme, line, column)
    }

    private fun reservedTokenType(lexeme: String): TokenType {
        return when (lexeme) {
            "@let" -> TokenType.LET
            "@if" -> TokenType.IF
            "@else" -> TokenType.ELSE
            "@while" -> TokenType.WHILE
            "@print" -> TokenType.PRINT
            "@return" -> TokenType.RETURN
            "@array" -> TokenType.ARRAY
            "@new" -> TokenType.NEW
            else -> TokenType.ERROR
        }
    }

    private fun messageForError(lexeme: String): String {
        return when {
            lexeme == "@" ->
                "Palabra reservada incompleta. Después de '@' debe venir una palabra reservada válida."

            lexeme.startsWith("@") ->
                "Palabra reservada inválida '$lexeme'. No pertenece al lenguaje KAJ."

            lexeme == "." ->
                "Identificador incompleto. Después de '.' debe venir una letra o '_'."

            lexeme.startsWith(".") ->
                "Identificador inválido '$lexeme'. Los identificadores deben iniciar con '.' seguido de una letra o '_'."

            lexeme.startsWith("\"") ->
                "Hilera inválida '$lexeme'. Falta la comilla doble de cierre."

            lexeme.endsWith(".") && lexeme.any { it.isDigit() } ->
                "Número decimal inválido '$lexeme'. Debe haber dígitos después del punto."

            lexeme == "&" ->
                "Operador lógico incompleto. Use '&&'."

            lexeme == "|" ->
                "Operador lógico incompleto. Use '||'."

            else ->
                "Lexema inválido '$lexeme'. Este símbolo o secuencia no pertenece al lenguaje KAJ."
        }
    }

    private fun errorToken(
        lexeme: String,
        line: Int,
        column: Int,
        message: String
    ): Token {
        return Token(TokenType.ERROR, lexeme, line, column, message)
    }

    private fun peek(): Char {
        return source[position]
    }

    private fun advance(): Char {
        val current = source[position++]

        if (current == '\n') {
            line++
            column = 1
        } else {
            column++
        }

        return current
    }

    private fun isAtEnd(): Boolean {
        return position >= source.length
    }
}