package edu.ic5701.parser

import edu.ic5701.ast.*
import edu.ic5701.scanner.Token
import edu.ic5701.scanner.TokenType

class Parser(private val tokens: List<Token>) {
    private var current = 0
    private val errors = mutableListOf<SyntaxError>()

    fun parse(): ParseResult {
        val program = program()
        consume(TokenType.EOF, "Se esperaba EOF al final del programa.")

        return if (errors.isEmpty()) {
            ParseResult(program, errors)
        } else {
            ParseResult(null, errors)
        }
    }

    private fun program(): Program {
        val functions = mutableListOf<FuncDecl>()

        while (!isAtEnd()) {
            functions.add(function())
        }

        return Program(functions)
    }

    private fun function(): FuncDecl {
        val name = consume(TokenType.IDENTIFIER, "Se esperaba el nombre de la funcion.")
        consume(TokenType.LPAREN, "Se esperaba '(' despues del nombre de la funcion.")

        val params = parameterList()

        consume(TokenType.RPAREN, "Se esperaba ')' despues de los parametros.")
        val body = block()

        return FuncDecl(name, params, body)
    }

    private fun parameterList(): List<Param> {
        val params = mutableListOf<Param>()

        if (check(TokenType.RPAREN)) return params

        do {
            val name = consume(TokenType.IDENTIFIER, "Se esperaba el nombre del parametro.")
            params.add(Param(name))
        } while (match(TokenType.COMMA))

        return params
    }

    private fun block(): Block {
        consume(TokenType.LBRACKET, "Se esperaba '[' al inicio del bloque.")

        val statements = mutableListOf<Stmt>()

        while (!check(TokenType.RBRACKET) && !isAtEnd()) {
            statements.add(statement())
        }

        consume(TokenType.RBRACKET, "Se esperaba ']' al final del bloque.")

        return Block(statements)
    }

    private fun statement(): Stmt {
        return when {
            match(TokenType.LET) -> declaration()
            match(TokenType.ARRAY) -> arrayDeclaration()
            match(TokenType.IF) -> ifStatement()
            match(TokenType.WHILE) -> whileStatement()
            match(TokenType.PRINT) -> printStatement()
            match(TokenType.RETURN) -> returnStatement()
            check(TokenType.IDENTIFIER) -> identifierStatement()
            else -> {
                error(peek(), "Se esperaba una sentencia.")
                advance()
                ExprStmt(IdentExpr(previous()))
            }
        }
    }

    private fun declaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Se esperaba un identificador despues de @let.")
        consume(TokenType.ASSIGN, "Se esperaba '=' en la declaracion.")
        val init = expression()
        consume(TokenType.SEMICOLON, "Se esperaba ';' al final de la declaracion.")

        return VarDecl(name, init)
    }

    private fun arrayDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Se esperaba un identificador despues de @array.")
        consume(TokenType.ASSIGN, "Se esperaba '=' en la declaracion de arreglo.")
        consume(TokenType.NEW, "Se esperaba @new en la declaracion de arreglo.")
        consume(TokenType.LPAREN, "Se esperaba '(' despues de @new.")

        val size = expression()

        consume(TokenType.RPAREN, "Se esperaba ')' despues del tamano del arreglo.")
        consume(TokenType.SEMICOLON, "Se esperaba ';' al final de la declaracion de arreglo.")

        return ArrayDecl(name, size)
    }

    private fun identifierStatement(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Se esperaba un identificador.")

        return when {
            match(TokenType.LBRACKET) -> {
                val index = expression()
                consume(TokenType.RBRACKET, "Se esperaba ']' despues del indice del arreglo.")

                val op = assignmentOperator()
                val value = expression()

                consume(TokenType.SEMICOLON, "Se esperaba ';' al final de la asignacion al arreglo.")

                ArrayAssignStmt(name, index, op, value)
            }

            isAssignmentOperator() -> {
                val op = assignmentOperator()
                val value = expression()

                consume(TokenType.SEMICOLON, "Se esperaba ';' al final de la asignacion.")

                AssignStmt(name, op, value)
            }

            match(TokenType.LPAREN) -> {
                val args = argumentList()
                consume(TokenType.RPAREN, "Se esperaba ')' despues de los argumentos.")
                consume(TokenType.SEMICOLON, "Se esperaba ';' despues de la llamada a funcion.")

                ExprStmt(CallExpr(name, args))
            }

            else -> {
                error(peek(), "Se esperaba asignacion, acceso a arreglo o llamada a funcion.")
                ExprStmt(IdentExpr(name))
            }
        }
    }

    private fun ifStatement(): Stmt {
        consume(TokenType.LPAREN, "Se esperaba '(' despues de @if.")
        val condition = expression()
        consume(TokenType.RPAREN, "Se esperaba ')' despues de la condicion.")

        val thenBlock = block()

        val elseBlock = if (match(TokenType.ELSE)) {
            block()
        } else {
            null
        }

        return IfStmt(condition, thenBlock, elseBlock)
    }

    private fun whileStatement(): Stmt {
        consume(TokenType.LPAREN, "Se esperaba '(' despues de @while.")
        val condition = expression()
        consume(TokenType.RPAREN, "Se esperaba ')' despues de la condicion.")

        val body = block()

        return WhileStmt(condition, body)
    }

    private fun printStatement(): Stmt {
        consume(TokenType.LPAREN, "Se esperaba '(' despues de @print.")
        val expr = expression()
        consume(TokenType.RPAREN, "Se esperaba ')' despues de la expresion de @print.")
        consume(TokenType.SEMICOLON, "Se esperaba ';' despues de @print.")

        return PrintStmt(expr)
    }

    private fun returnStatement(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Se esperaba ';' despues de @return.")

        return ReturnStmt(expr)
    }

    private fun expression(): Expr = logicOr()

    private fun logicOr(): Expr {
        var expr = logicAnd()

        while (match(TokenType.OR)) {
            val right = logicAnd()
            expr = BinaryExpr(expr, BinaryOp.OR, right)
        }

        return expr
    }

    private fun logicAnd(): Expr {
        var expr = equality()

        while (match(TokenType.AND)) {
            val right = equality()
            expr = BinaryExpr(expr, BinaryOp.AND, right)
        }

        return expr
    }

    private fun equality(): Expr {
        var expr = relational()

        while (match(TokenType.EQ) || match(TokenType.NE)) {
            val op = previous()
            val right = relational()

            expr = BinaryExpr(
                expr,
                if (op.type == TokenType.EQ) BinaryOp.EQ else BinaryOp.NE,
                right
            )
        }

        return expr
    }

    private fun relational(): Expr {
        var expr = term()

        while (
            match(TokenType.LT) ||
            match(TokenType.GT) ||
            match(TokenType.LE) ||
            match(TokenType.GE)
        ) {
            val op = previous()
            val right = term()

            val binaryOp = when (op.type) {
                TokenType.LT -> BinaryOp.LT
                TokenType.GT -> BinaryOp.GT
                TokenType.LE -> BinaryOp.LE
                TokenType.GE -> BinaryOp.GE
                else -> BinaryOp.LT
            }

            expr = BinaryExpr(expr, binaryOp, right)
        }

        return expr
    }

    private fun term(): Expr {
        var expr = factor()

        while (match(TokenType.PLUS) || match(TokenType.MINUS)) {
            val op = previous()
            val right = factor()

            expr = BinaryExpr(
                expr,
                if (op.type == TokenType.PLUS) BinaryOp.ADD else BinaryOp.SUB,
                right
            )
        }

        return expr
    }

    private fun factor(): Expr {
        var expr = unary()

        while (match(TokenType.STAR) || match(TokenType.SLASH)) {
            val op = previous()
            val right = unary()

            expr = BinaryExpr(
                expr,
                if (op.type == TokenType.STAR) BinaryOp.MUL else BinaryOp.DIV,
                right
            )
        }

        return expr
    }

    private fun unary(): Expr {
        return when {
            match(TokenType.NOT) -> UnaryExpr(UnaryOp.NOT, unary())
            match(TokenType.MINUS) -> UnaryExpr(UnaryOp.NEG, unary())
            else -> primary()
        }
    }

    private fun primary(): Expr {
        return when {
            match(TokenType.NUMBER) -> NumberExpr(previous())
            match(TokenType.CHAR) -> CharExpr(previous())
            match(TokenType.STRING) -> StringExpr(previous())

            match(TokenType.IDENTIFIER) -> {
                val name = previous()

                when {
                    match(TokenType.LPAREN) -> {
                        val args = argumentList()
                        consume(TokenType.RPAREN, "Se esperaba ')' despues de los argumentos.")
                        CallExpr(name, args)
                    }

                    match(TokenType.LBRACKET) -> {
                        val index = expression()
                        consume(TokenType.RBRACKET, "Se esperaba ']' despues del indice del arreglo.")
                        ArrayAccessExpr(name, index)
                    }

                    else -> IdentExpr(name)
                }
            }

            match(TokenType.LPAREN) -> {
                val expr = expression()
                consume(TokenType.RPAREN, "Se esperaba ')' despues de la expresion.")
                expr
            }

            else -> {
                error(peek(), "Se esperaba una expresion.")
                val token = advance()
                IdentExpr(token)
            }
        }
    }

    private fun argumentList(): List<Expr> {
        val args = mutableListOf<Expr>()

        if (check(TokenType.RPAREN)) return args

        do {
            args.add(expression())
        } while (match(TokenType.COMMA))

        return args
    }

    private fun assignmentOperator(): AssignOp {
        return when {
            match(TokenType.ASSIGN) -> AssignOp.ASSIGN
            match(TokenType.PLUS_ASSIGN) -> AssignOp.PLUS_ASSIGN
            match(TokenType.MINUS_ASSIGN) -> AssignOp.MINUS_ASSIGN
            match(TokenType.MUL_ASSIGN) -> AssignOp.MUL_ASSIGN
            match(TokenType.DIV_ASSIGN) -> AssignOp.DIV_ASSIGN
            else -> {
                error(peek(), "Se esperaba un operador de asignacion.")
                AssignOp.ASSIGN
            }
        }
    }

    private fun isAssignmentOperator(): Boolean {
        return check(TokenType.ASSIGN) ||
                check(TokenType.PLUS_ASSIGN) ||
                check(TokenType.MINUS_ASSIGN) ||
                check(TokenType.MUL_ASSIGN) ||
                check(TokenType.DIV_ASSIGN)
    }

    private fun match(type: TokenType): Boolean {
        if (check(type)) {
            advance()
            return true
        }

        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()

        error(peek(), message)
        return peek()
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return type == TokenType.EOF
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type == TokenType.EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun error(token: Token, message: String) {
        errors.add(
            SyntaxError(
                message = message,
                line = token.line,
                column = token.column
            )
        )
    }
}