package edu.ic5701.gen

import edu.ic5701.ast.*

class CodeGenerator : AstVisitor<Unit> {
    private val instructions = mutableListOf<String>()
    private val slots = mutableMapOf<String, Int>()

    private var labelCounter = 0
    private var nextSlot = 0
    private var currentFunctionName = ""

    fun generate(program: Program): String {
        instructions.clear()
        slots.clear()
        labelCounter = 0
        nextSlot = 0
        currentFunctionName = ""

        emitRuntimePrelude()

        program.accept(this)

        return instructions.joinToString(System.lineSeparator()) + System.lineSeparator()
    }

    override fun visit(program: Program) {
        program.functions.forEachIndexed { index, function ->
            function.accept(this)

            if (index != program.functions.lastIndex) {
                emit("")
            }
        }
    }

    override fun visit(funcDecl: FuncDecl) {
        slots.clear()
        nextSlot = 0
        currentFunctionName = functionName(funcDecl.name.lexeme)

        funcDecl.params.forEachIndexed { index, param ->
            slots[param.name.lexeme] = index
        }

        nextSlot = funcDecl.params.size

        val localCount = countLocals(funcDecl.body)

        emit(".def $currentFunctionName: args=${funcDecl.params.size}, locals=$localCount")
        funcDecl.body.accept(this)

        if (currentFunctionName == "main") {
            emitInstruction("halt")
        } else if (!endsWithReturn(funcDecl.body)) {
            emitInstruction("ret")
        }
    }

    override fun visit(param: Param) {
        // Los parametros se registran al entrar a la funcion.
    }

    override fun visit(block: Block) {
        block.statements.forEach { statement ->
            statement.accept(this)
        }
    }

    override fun visit(varDecl: VarDecl) {
        val slot = defineSlot(varDecl.name.lexeme)

        varDecl.init.accept(this)
        emitInstruction("store $slot ; ${cleanName(varDecl.name.lexeme)}")
    }

    override fun visit(arrayDecl: ArrayDecl) {
        val slot = defineSlot(arrayDecl.name.lexeme)

        arrayDecl.size.accept(this)
        emitInstruction("alloc")
        emitInstruction("store $slot ; ${cleanName(arrayDecl.name.lexeme)}")
    }

    override fun visit(assignStmt: AssignStmt) {
        val slot = resolveSlot(assignStmt.name.lexeme)

        when (assignStmt.op) {
            AssignOp.ASSIGN -> {
                assignStmt.value.accept(this)
            }

            AssignOp.PLUS_ASSIGN,
            AssignOp.MINUS_ASSIGN,
            AssignOp.MUL_ASSIGN,
            AssignOp.DIV_ASSIGN -> {
                emitInstruction("load $slot ; ${cleanName(assignStmt.name.lexeme)}")
                assignStmt.value.accept(this)
                emitInstruction(arithmeticInstruction(assignStmt.op, assignStmt.value.type))
            }
        }

        emitInstruction("store $slot ; ${cleanName(assignStmt.name.lexeme)}")
    }

    override fun visit(arrayAssignStmt: ArrayAssignStmt) {
        val slot = resolveSlot(arrayAssignStmt.name.lexeme)

        when (arrayAssignStmt.op) {
            AssignOp.ASSIGN -> {
                arrayAssignStmt.value.accept(this)
                arrayAssignStmt.index.accept(this)
                emitInstruction("hstore $slot ; ${cleanName(arrayAssignStmt.name.lexeme)}")
            }

            AssignOp.PLUS_ASSIGN,
            AssignOp.MINUS_ASSIGN,
            AssignOp.MUL_ASSIGN,
            AssignOp.DIV_ASSIGN -> {
                arrayAssignStmt.index.accept(this)
                emitInstruction("dup")
                emitInstruction("hiload $slot ; ${cleanName(arrayAssignStmt.name.lexeme)}")
                arrayAssignStmt.value.accept(this)
                emitInstruction(arithmeticInstruction(arrayAssignStmt.op, arrayAssignStmt.value.type))
                emitInstruction("swap")
                emitInstruction("hstore $slot ; ${cleanName(arrayAssignStmt.name.lexeme)}")
            }
        }
    }

    override fun visit(ifStmt: IfStmt) {
        val elseLabel = newLabel("else")
        val endLabel = newLabel("end")

        ifStmt.condition.accept(this)
        emitInstruction("jmpf $elseLabel")

        ifStmt.thenBlock.accept(this)

        if (ifStmt.elseBlock != null) {
            emitInstruction("jmp $endLabel")
            emit("$elseLabel:")
            ifStmt.elseBlock.accept(this)
            emit("$endLabel:")
        } else {
            emit("$elseLabel:")
        }
    }

    override fun visit(whileStmt: WhileStmt) {
        val startLabel = newLabel("while")
        val endLabel = newLabel("endwhile")

        emit("$startLabel:")
        whileStmt.condition.accept(this)
        emitInstruction("jmpf $endLabel")

        whileStmt.body.accept(this)
        emitInstruction("jmp $startLabel")

        emit("$endLabel:")
    }

    override fun visit(printStmt: PrintStmt) {
        printStmt.expr.accept(this)
        emitInstruction("print")
    }

    override fun visit(returnStmt: ReturnStmt) {
        returnStmt.expr.accept(this)
        emitInstruction("ret")
    }

    override fun visit(exprStmt: ExprStmt) {
        exprStmt.expr.accept(this)

        if (exprStmt.expr is CallExpr) {
            emitInstruction("pop")
        }
    }

    override fun visit(numberExpr: NumberExpr) {
        if (numberExpr.value.lexeme.contains(".")) {
            emitInstruction("fconst ${numberExpr.value.lexeme}")
        } else {
            emitInstruction("iconst ${numberExpr.value.lexeme}")
        }
    }

    override fun visit(charExpr: CharExpr) {
        emitInstruction("cconst ${numberExprToCharLiteral(charExpr.value.lexeme)}")
    }

    override fun visit(stringExpr: StringExpr) {
        emitInstruction("sconst ${stringExpr.value.lexeme}")
    }

    override fun visit(identExpr: IdentExpr) {
        val slot = resolveSlot(identExpr.name.lexeme)
        emitInstruction("load $slot ; ${cleanName(identExpr.name.lexeme)}")
    }

    override fun visit(callExpr: CallExpr) {
        callExpr.args.forEach { arg ->
            arg.accept(this)
        }

        emitInstruction("call ${functionName(callExpr.callee.lexeme)}()")
    }

    override fun visit(arrayAccessExpr: ArrayAccessExpr) {
        val slot = resolveSlot(arrayAccessExpr.name.lexeme)

        if (arrayAccessExpr.name.lexeme == ".palabra") {
            emitInstruction("load $slot")
            arrayAccessExpr.index.accept(this)
            emitInstruction("sget")
            emitInstruction("ctoi")
            return
        }

        arrayAccessExpr.index.accept(this)

        when (arrayAccessExpr.type) {
            KajType.FLOAT -> emitInstruction("hfload $slot")
            KajType.CHAR -> emitInstruction("hcload $slot")
            KajType.STRING -> emitInstruction("hsload $slot")
            else -> emitInstruction("hiload $slot")
        }
    }

    override fun visit(binaryExpr: BinaryExpr) {
        when (binaryExpr.op) {
            BinaryOp.GT -> {
                binaryExpr.right.accept(this)
                binaryExpr.left.accept(this)
                emitInstruction(lessThanInstruction(operationType(binaryExpr)))
            }

            BinaryOp.LE -> {
                binaryExpr.right.accept(this)
                binaryExpr.left.accept(this)
                emitInstruction(lessThanInstruction(operationType(binaryExpr)))
                emitInstruction("iconst 0")
                emitInstruction("ieq")
            }

            BinaryOp.GE -> {
                binaryExpr.left.accept(this)
                binaryExpr.right.accept(this)
                emitInstruction(lessThanInstruction(operationType(binaryExpr)))
                emitInstruction("iconst 0")
                emitInstruction("ieq")
            }

            BinaryOp.NE -> {
                binaryExpr.left.accept(this)
                binaryExpr.right.accept(this)
                emitInstruction(equalsInstruction(operationType(binaryExpr)))
                emitInstruction("iconst 0")
                emitInstruction("ieq")
            }

            BinaryOp.AND -> {
                binaryExpr.left.accept(this)
                binaryExpr.right.accept(this)
                emitInstruction("imul")
            }

            BinaryOp.OR -> {
                binaryExpr.left.accept(this)
                binaryExpr.right.accept(this)
                emitInstruction("iadd")
                emitInstruction("iconst 0")
                emitInstruction("ieq")
                emitInstruction("iconst 0")
                emitInstruction("ieq")
            }

            else -> {
                binaryExpr.left.accept(this)
                binaryExpr.right.accept(this)

                val type = operationType(binaryExpr)

                val instruction = when (binaryExpr.op) {
                    BinaryOp.ADD -> plusInstruction(type)
                    BinaryOp.SUB -> minusInstruction(type)
                    BinaryOp.MUL -> multiplyInstruction(type)
                    BinaryOp.DIV -> divideInstruction(type)
                    BinaryOp.LT -> lessThanInstruction(type)
                    BinaryOp.EQ -> equalsInstruction(type)
                    else -> error("Operador binario no soportado: ${binaryExpr.op}")
                }

                emitInstruction(instruction)
            }
        }
    }

    override fun visit(unaryExpr: UnaryExpr) {
        when (unaryExpr.op) {
            UnaryOp.NOT -> {
                unaryExpr.expr.accept(this)
                emitInstruction("iconst 0")
                emitInstruction("ieq")
            }

            UnaryOp.NEG -> {
                val type = numericTypeOf(unaryExpr.expr)

                emitInstruction(if (type == KajType.FLOAT) "fconst 0.0" else "iconst 0")
                unaryExpr.expr.accept(this)
                emitInstruction(if (type == KajType.FLOAT) "fsub" else "isub")
            }
        }
    }

    private fun defineSlot(name: String): Int {
        return slots.getOrPut(name) {
            val slot = nextSlot
            nextSlot++
            slot
        }
    }

    private fun resolveSlot(name: String): Int {
        return slots[name] ?: defineSlot(name)
    }

    private fun countLocals(block: Block): Int {
        var count = 0

        block.statements.forEach { statement ->
            count += when (statement) {
                is VarDecl -> 1
                is ArrayDecl -> 1
                is IfStmt -> countLocals(statement.thenBlock) + (statement.elseBlock?.let { countLocals(it) } ?: 0)
                is WhileStmt -> countLocals(statement.body)
                else -> 0
            }
        }

        return count
    }

    private fun endsWithReturn(block: Block): Boolean {
        return block.statements.lastOrNull() is ReturnStmt
    }

    private fun operationType(binaryExpr: BinaryExpr): KajType {

        return when {
            binaryExpr.type == KajType.FLOAT -> KajType.FLOAT
            binaryExpr.left.type == KajType.FLOAT -> KajType.FLOAT
            binaryExpr.right.type == KajType.FLOAT -> KajType.FLOAT
            numericTypeOf(binaryExpr.left) == KajType.FLOAT -> KajType.FLOAT
            numericTypeOf(binaryExpr.right) == KajType.FLOAT -> KajType.FLOAT
            binaryExpr.type == KajType.INT && binaryExpr.left.type == KajType.INT && binaryExpr.right.type == KajType.INT -> KajType.INT
            binaryExpr.left.type == KajType.INT && binaryExpr.right.type == KajType.INT -> KajType.INT
            else -> KajType.INT
        }
    }

    private fun numericTypeOf(expr: Expr): KajType? {
        return when (expr) {
            is NumberExpr -> {
                if (expr.value.lexeme.contains(".")) {
                    KajType.FLOAT
                } else {
                    KajType.INT
                }
            }

            is BinaryExpr -> operationType(expr)

            is UnaryExpr -> numericTypeOf(expr.expr)

            is CallExpr -> expr.type

            is ArrayAccessExpr -> expr.type

            is IdentExpr -> expr.type

            else -> expr.type
        }
    }

    private fun plusInstruction(type: KajType?): String {
        return if (type == KajType.FLOAT) "fadd" else "iadd"
    }

    private fun minusInstruction(type: KajType?): String {
        return if (type == KajType.FLOAT) "fsub" else "isub"
    }

    private fun multiplyInstruction(type: KajType?): String {
        return if (type == KajType.FLOAT) "fmul" else "imul"
    }

    private fun divideInstruction(type: KajType?): String {
        return if (type == KajType.FLOAT) "call fdiv()" else "call idiv()"
    }

    private fun lessThanInstruction(type: KajType?): String {
        return if (type == KajType.FLOAT) "flt" else "ilt"
    }

    private fun equalsInstruction(type: KajType?): String {
        return if (type == KajType.FLOAT) "feq" else "ieq"
    }

    private fun arithmeticInstruction(op: AssignOp, type: KajType?): String {
        return when (op) {
            AssignOp.PLUS_ASSIGN -> plusInstruction(type ?: KajType.FLOAT)
            AssignOp.MINUS_ASSIGN -> minusInstruction(type ?: KajType.FLOAT)
            AssignOp.MUL_ASSIGN -> multiplyInstruction(type ?: KajType.FLOAT)
            AssignOp.DIV_ASSIGN -> divideInstruction(type ?: KajType.FLOAT)
            AssignOp.ASSIGN -> error("El operador '=' no tiene instruccion aritmetica asociada.")
        }
    }

    private fun functionName(name: String): String {
        return name.removePrefix(".")
    }

    private fun isStringLengthCall(callExpr: CallExpr): Boolean {
        return functionName(callExpr.callee.lexeme) == "slen"
    }

    private fun cleanName(name: String): String {
        return name.removePrefix(".")
    }

    private fun numberExprToCharLiteral(lexeme: String): String {
        return lexeme
    }

    private fun newLabel(prefix: String): String {
        labelCounter++
        return "${prefix}_$labelCounter"
    }

    private fun emitRuntimePrelude() {
        val runtime = javaClass.classLoader
            .getResourceAsStream("stackvm_division.txt")
            ?.bufferedReader()
            ?.readText()
            ?: error("No se encontro el recurso stackvm_division.txt")

        instructions.addAll(runtime.lines())

        if (instructions.lastOrNull()?.isNotBlank() == true) {
            emit("")
        }
    }

    private fun emit(line: String) {
        instructions.add(line)
    }

    private fun emitInstruction(line: String) {
        instructions.add("\t$line")
    }
}
