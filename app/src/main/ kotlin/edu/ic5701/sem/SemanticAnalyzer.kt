package edu.ic5701.sem

import edu.ic5701.ast.*
import edu.ic5701.scanner.Token

class SemanticAnalyzer : AstVisitor<KajType?> {
    private val errors = mutableListOf<SemanticError>()
    private val symbols = SymbolTable()
    private var currentReturnTypes = mutableListOf<KajType?>()

    fun analyze(program: Program): List<SemanticError> {
        program.accept(this)
        return errors
    }

    override fun visit(program: Program): KajType? {
        program.functions.forEach { function ->
            val ok = symbols.define(
                Symbol(function.name.lexeme, null, SymbolCategory.FUNC, function)
            )

            if (!ok) {
                semanticError(function.name, "Funcion '${function.name.lexeme}' ya declarada.")
            }
        }

        if (symbols.resolve(".main") == null) {
            errors.add(SemanticError("No existe funcion .main.", 1, 1))
        }

        program.functions.forEach { it.accept(this) }
        return null
    }

    override fun visit(funcDecl: FuncDecl): KajType? {
        currentReturnTypes = mutableListOf()

        symbols.newScope()

        funcDecl.params.forEach { it.accept(this) }
        funcDecl.body.accept(this)

        val returnType = currentReturnTypes.firstOrNull { it != null }
        symbols.resolve(funcDecl.name.lexeme)?.type = returnType

        symbols.popScope()
        return null
    }

    override fun visit(param: Param): KajType? {
        val ok = symbols.define(
            Symbol(param.name.lexeme, null, SymbolCategory.PARAM)
        )

        if (!ok) {
            semanticError(param.name, "Parametro '${param.name.lexeme}' ya declarado.")
        }

        return null
    }

    override fun visit(block: Block): KajType? {
        symbols.newScope()
        block.statements.forEach { it.accept(this) }
        symbols.popScope()
        return null
    }

    override fun visit(varDecl: VarDecl): KajType? {
        val type = varDecl.init.accept(this)

        val ok = symbols.define(
            Symbol(varDecl.name.lexeme, type, SymbolCategory.VAR)
        )

        if (!ok) {
            semanticError(varDecl.name, "Variable '${varDecl.name.lexeme}' ya declarada.")
        }

        return null
    }

    override fun visit(arrayDecl: ArrayDecl): KajType? {
        val sizeType = arrayDecl.size.accept(this)

        if (sizeType != null && sizeType != KajType.INT) {
            semanticError(arrayDecl.name, "El tamano del arreglo debe ser int.")
        }

        val ok = symbols.define(
            Symbol(arrayDecl.name.lexeme, KajType.ARRAY, SymbolCategory.ARRAY)
        )

        if (!ok) {
            semanticError(arrayDecl.name, "Arreglo '${arrayDecl.name.lexeme}' ya declarado.")
        }

        return null
    }

    override fun visit(assignStmt: AssignStmt): KajType? {
        val symbol = symbols.resolve(assignStmt.name.lexeme)

        if (symbol == null) {
            semanticError(assignStmt.name, "Variable '${assignStmt.name.lexeme}' no declarada.")
        }

        val valueType = assignStmt.value.accept(this)

        if (symbol != null && symbol.type == null) {
            symbol.type = valueType
        }

        return null
    }

    override fun visit(arrayAssignStmt: ArrayAssignStmt): KajType? {
        val symbol = symbols.resolve(arrayAssignStmt.name.lexeme)

        if (symbol == null) {
            semanticError(arrayAssignStmt.name, "Arreglo '${arrayAssignStmt.name.lexeme}' no declarado.")
        } else if (symbol.category != SymbolCategory.ARRAY) {
            semanticError(arrayAssignStmt.name, "'${arrayAssignStmt.name.lexeme}' no es un arreglo.")
        }

        val indexType = arrayAssignStmt.index.accept(this)

        if (indexType != null && indexType != KajType.INT) {
            semanticError(arrayAssignStmt.name, "El indice del arreglo debe ser int.")
        }

        arrayAssignStmt.value.accept(this)

        return null
    }

    override fun visit(ifStmt: IfStmt): KajType? {
        val conditionType = ifStmt.condition.accept(this)

        if (conditionType != null && conditionType != KajType.BOOL) {
            semanticErrorFromExpr(ifStmt.condition, "La condicion del if debe ser bool.")
        }

        ifStmt.thenBlock.accept(this)
        ifStmt.elseBlock?.accept(this)

        return null
    }

    override fun visit(whileStmt: WhileStmt): KajType? {
        val conditionType = whileStmt.condition.accept(this)

        if (conditionType != null && conditionType != KajType.BOOL) {
            semanticErrorFromExpr(whileStmt.condition, "La condicion del while debe ser bool.")
        }

        whileStmt.body.accept(this)

        return null
    }

    override fun visit(printStmt: PrintStmt): KajType? {
        printStmt.expr.accept(this)
        return null
    }

    override fun visit(returnStmt: ReturnStmt): KajType? {
        val returnType = returnStmt.expr.accept(this)
        currentReturnTypes.add(returnType)
        return null
    }

    override fun visit(exprStmt: ExprStmt): KajType? {
        exprStmt.expr.accept(this)
        return null
    }

    override fun visit(numberExpr: NumberExpr): KajType? {
        numberExpr.type =
            if (numberExpr.value.lexeme.contains(".")) KajType.FLOAT else KajType.INT
        return numberExpr.type
    }

    override fun visit(charExpr: CharExpr): KajType? {
        charExpr.type = KajType.CHAR
        return charExpr.type
    }

    override fun visit(stringExpr: StringExpr): KajType? {
        stringExpr.type = KajType.STRING
        return stringExpr.type
    }

    override fun visit(identExpr: IdentExpr): KajType? {
        val symbol = symbols.resolve(identExpr.name.lexeme)

        if (symbol == null) {
            semanticError(identExpr.name, "Identificador '${identExpr.name.lexeme}' no declarado.")
            return null
        }

        identExpr.type = symbol.type
        return identExpr.type
    }

    override fun visit(callExpr: CallExpr): KajType? {
        val symbol = symbols.resolve(callExpr.callee.lexeme)

        if (symbol == null) {
            semanticError(callExpr.callee, "Funcion '${callExpr.callee.lexeme}' no declarada.")
        } else if (symbol.category != SymbolCategory.FUNC) {
            semanticError(callExpr.callee, "'${callExpr.callee.lexeme}' no es una funcion.")
        } else {
            val expected = symbol.function?.params?.size ?: 0

            if (callExpr.args.size != expected) {
                semanticError(
                    callExpr.callee,
                    "La funcion '${callExpr.callee.lexeme}' espera $expected argumentos, pero recibio ${callExpr.args.size}."
                )
            }
        }

        callExpr.args.forEach { it.accept(this) }

        callExpr.type = symbol?.type
        return callExpr.type
    }

    override fun visit(arrayAccessExpr: ArrayAccessExpr): KajType? {
        val symbol = symbols.resolve(arrayAccessExpr.name.lexeme)

        if (symbol == null) {
            semanticError(arrayAccessExpr.name, "Arreglo '${arrayAccessExpr.name.lexeme}' no declarado.")
        } else if (symbol.category != SymbolCategory.ARRAY) {
            semanticError(arrayAccessExpr.name, "'${arrayAccessExpr.name.lexeme}' no es un arreglo.")
        }

        val indexType = arrayAccessExpr.index.accept(this)

        if (indexType != null && indexType != KajType.INT) {
            semanticError(arrayAccessExpr.name, "El indice del arreglo debe ser int.")
        }

        arrayAccessExpr.type = KajType.INT
        return arrayAccessExpr.type
    }

    override fun visit(binaryExpr: BinaryExpr): KajType? {
        val leftType = binaryExpr.left.accept(this)
        val rightType = binaryExpr.right.accept(this)

        when (binaryExpr.op) {
            BinaryOp.ADD, BinaryOp.SUB, BinaryOp.MUL, BinaryOp.DIV -> {
                if (leftType != null && rightType != null) {
                    if (!isNumeric(leftType) || !isNumeric(rightType)) {
                        semanticErrorFromExpr(binaryExpr, "Los operadores aritmeticos requieren int o float.")
                    }

                    if (leftType != rightType) {
                        semanticErrorFromExpr(binaryExpr, "Los operandos aritmeticos deben tener el mismo tipo.")
                    }
                }

                binaryExpr.type = leftType ?: rightType
            }

            BinaryOp.LT, BinaryOp.GT, BinaryOp.LE, BinaryOp.GE -> {
                if (leftType != null && !isNumeric(leftType)) {
                    semanticErrorFromExpr(binaryExpr, "Los operadores relacionales requieren int o float.")
                }

                if (rightType != null && !isNumeric(rightType)) {
                    semanticErrorFromExpr(binaryExpr, "Los operadores relacionales requieren int o float.")
                }

                binaryExpr.type = KajType.BOOL
            }

            BinaryOp.EQ, BinaryOp.NE -> {
                if (leftType != null && rightType != null && leftType != rightType) {
                    semanticErrorFromExpr(binaryExpr, "Los operandos de igualdad deben tener el mismo tipo.")
                }

                binaryExpr.type = KajType.BOOL
            }

            BinaryOp.AND, BinaryOp.OR -> {
                if (leftType != null && leftType != KajType.BOOL) {
                    semanticErrorFromExpr(binaryExpr, "Los operadores logicos requieren bool.")
                }

                if (rightType != null && rightType != KajType.BOOL) {
                    semanticErrorFromExpr(binaryExpr, "Los operadores logicos requieren bool.")
                }

                binaryExpr.type = KajType.BOOL
            }
        }

        return binaryExpr.type
    }

    override fun visit(unaryExpr: UnaryExpr): KajType? {
        val exprType = unaryExpr.expr.accept(this)

        when (unaryExpr.op) {
            UnaryOp.NEG -> {
                if (exprType != null && !isNumeric(exprType)) {
                    semanticErrorFromExpr(unaryExpr, "El operador '-' requiere int o float.")
                }

                unaryExpr.type = exprType
            }

            UnaryOp.NOT -> {
                if (exprType != null && exprType != KajType.BOOL) {
                    semanticErrorFromExpr(unaryExpr, "El operador '!' requiere bool.")
                }

                unaryExpr.type = KajType.BOOL
            }
        }

        return unaryExpr.type
    }

    private fun isNumeric(type: KajType?): Boolean {
        return type == KajType.INT || type == KajType.FLOAT
    }

    private fun semanticError(token: Token, message: String) {
        errors.add(SemanticError(message, token.line, token.column))
    }

    private fun semanticErrorFromExpr(expr: Expr, message: String) {
        val token = tokenFromExpr(expr)
        errors.add(SemanticError(message, token?.line ?: 0, token?.column ?: 0))
    }

    private fun tokenFromExpr(expr: Expr): Token? {
        return when (expr) {
            is NumberExpr -> expr.value
            is CharExpr -> expr.value
            is StringExpr -> expr.value
            is IdentExpr -> expr.name
            is ArrayAccessExpr -> expr.name
            is CallExpr -> expr.callee
            is BinaryExpr -> tokenFromExpr(expr.left)
            is UnaryExpr -> tokenFromExpr(expr.expr)
        }
    }
}