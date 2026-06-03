package edu.ic5701.ast

import edu.ic5701.scanner.Token

interface AstVisitor<R> {
    fun visit(program: Program): R
    fun visit(funcDecl: FuncDecl): R
    fun visit(param: Param): R
    fun visit(block: Block): R

    fun visit(varDecl: VarDecl): R
    fun visit(arrayDecl: ArrayDecl): R
    fun visit(assignStmt: AssignStmt): R
    fun visit(arrayAssignStmt: ArrayAssignStmt): R
    fun visit(ifStmt: IfStmt): R
    fun visit(whileStmt: WhileStmt): R
    fun visit(printStmt: PrintStmt): R
    fun visit(returnStmt: ReturnStmt): R
    fun visit(exprStmt: ExprStmt): R

    fun visit(numberExpr: NumberExpr): R
    fun visit(charExpr: CharExpr): R
    fun visit(stringExpr: StringExpr): R
    fun visit(identExpr: IdentExpr): R
    fun visit(callExpr: CallExpr): R
    fun visit(arrayAccessExpr: ArrayAccessExpr): R
    fun visit(binaryExpr: BinaryExpr): R
    fun visit(unaryExpr: UnaryExpr): R
}

sealed interface AstNode {
    fun <R> accept(visitor: AstVisitor<R>): R
}

@Suppress("unused")
enum class KajType {
    INT, FLOAT, BOOL, CHAR, STRING, VOID, ARRAY
}

enum class AssignOp {
    ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, MUL_ASSIGN, DIV_ASSIGN
}

enum class BinaryOp {
    ADD, SUB, MUL, DIV, LT, GT, LE, GE, EQ, NE, AND, OR
}

enum class UnaryOp {
    NOT, NEG
}

data class Program(
    val functions: List<FuncDecl>
) : AstNode {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class FuncDecl(
    val name: Token,
    val params: List<Param>,
    val body: Block
) : AstNode {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class Param(
    val name: Token
) : AstNode {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class Block(
    val statements: List<Stmt>
) : AstNode {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

sealed interface Stmt : AstNode

data class VarDecl(
    val name: Token,
    val init: Expr
) : Stmt {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class ArrayDecl(
    val name: Token,
    val size: Expr
) : Stmt {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class AssignStmt(
    val name: Token,
    val op: AssignOp,
    val value: Expr
) : Stmt {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class ArrayAssignStmt(
    val name: Token,
    val index: Expr,
    val op: AssignOp,
    val value: Expr
) : Stmt {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class IfStmt(
    val condition: Expr,
    val thenBlock: Block,
    val elseBlock: Block?
) : Stmt {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class WhileStmt(
    val condition: Expr,
    val body: Block
) : Stmt {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class PrintStmt(
    val expr: Expr
) : Stmt {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class ReturnStmt(
    val expr: Expr
) : Stmt {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class ExprStmt(
    val expr: Expr
) : Stmt {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

sealed interface Expr : AstNode {
    var type: KajType?
}

data class NumberExpr(
    val value: Token,
    override var type: KajType? = null
) : Expr {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class CharExpr(
    val value: Token,
    override var type: KajType? = null
) : Expr {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class StringExpr(
    val value: Token,
    override var type: KajType? = null
) : Expr {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class IdentExpr(
    val name: Token,
    override var type: KajType? = null
) : Expr {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class CallExpr(
    val callee: Token,
    val args: List<Expr>,
    override var type: KajType? = null
) : Expr {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class ArrayAccessExpr(
    val name: Token,
    val index: Expr,
    override var type: KajType? = null
) : Expr {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class BinaryExpr(
    val left: Expr,
    val op: BinaryOp,
    val right: Expr,
    override var type: KajType? = null
) : Expr {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class UnaryExpr(
    val op: UnaryOp,
    val expr: Expr,
    override var type: KajType? = null
) : Expr {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}