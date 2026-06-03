package edu.ic5701

import edu.ic5701.gen.CodeGenerator
import edu.ic5701.parser.Parser
import edu.ic5701.scanner.Scanner
import edu.ic5701.scanner.TokenType
import edu.ic5701.sem.SemanticAnalyzer
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Uso: compiler <ruta-archivo>")
        return
    }

    val source = File(args[0]).readText()

    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()

    println("TOKENS RECONOCIDOS")
    println("------------------")

    tokens.forEach { token ->
        if (token.type == TokenType.ERROR) {
            println("${token.type} | '${token.lexeme}' | linea=${token.line} columna=${token.column} | ${token.message}")
        } else {
            println("${token.type} | '${token.lexeme}' | linea=${token.line} columna=${token.column}")
        }
    }

    println()
    println("ANALISIS")
    println("--------")

    val lexicalErrors = tokens.filter { it.type == TokenType.ERROR }

    if (lexicalErrors.isNotEmpty()) {
        println("Se encontraron errores lexicos:")

        lexicalErrors.forEach { token ->
            println("ERROR LEXICO | '${token.lexeme}' | linea=${token.line} columna=${token.column} | ${token.message}")
        }

        return
    }

    val parser = Parser(tokens)
    val result = parser.parse()

    if (!result.isSuccess) {
        println("Se encontraron errores sintacticos:")

        result.errors.forEach { error ->
            println("ERROR SINTACTICO | linea=${error.line} columna=${error.column} | ${error.message}")
        }

        return
    }

    println("El archivo fuente es valido.")
    println()
    println("AST PRODUCIDO")
    println("-------------")
    println(result.program)

    val semanticAnalyzer = SemanticAnalyzer()
    val semanticErrors = semanticAnalyzer.analyze(result.program!!)

    println()
    println("ANALISIS SEMANTICO")
    println("------------------")

    if (semanticErrors.isNotEmpty()) {
        println("Se encontraron errores semanticos:")

        semanticErrors.forEach { error ->
            println("ERROR SEMANTICO | linea=${error.line} columna=${error.column} | ${error.message}")
        }

        return
    }

    println("El programa no presenta errores semanticos.")

    val codeGenerator = CodeGenerator()
    val stackVmCode = codeGenerator.generate(result.program)

    val outputFile = File("out.stkasm")
    outputFile.writeText(stackVmCode)

    println()
    println("GENERACION DE CODIGO")
    println("--------------------")
    println("Codigo StackVM generado en: ${outputFile.absolutePath}")
}