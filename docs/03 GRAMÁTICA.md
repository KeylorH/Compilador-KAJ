# Gramática del lenguaje KAJ

## 1. Descripción general

La gramática del lenguaje KAJ describe cómo deben estructurarse correctamente los programas escritos en este lenguaje.
Fue diseñada para poder ser analizada mediante un parser LL(1), por lo que evita ambigüedades y no utiliza recursividad izquierda.

---

## 2. Convención utilizada

- Los símbolos terminales son tokens reconocidos por el analizador léxico.
- Los símbolos no terminales se escriben en mayúsculas.
- El símbolo `ε` representa vacío.

---

## 3. Símbolos terminales

Los símbolos terminales representan los tokens que reconoce el analizador léxico del lenguaje:

- IDENTIFIER
- NUMBER
- CHAR
- STRING
- LET
- IF
- ELSE
- WHILE
- PRINT
- RETURN
- ARRAY
- NEW
- ASSIGN (=)
- PLUS_ASSIGN (+=)
- MINUS_ASSIGN (-=)
- MUL_ASSIGN (*=)
- DIV_ASSIGN (/=)
- PLUS (+)
- MINUS (-)
- STAR (*)
- SLASH (/)
- LT (<)
- GT (>)
- LE (<=)
- GE (>=)
- EQ (==)
- NE (!=)
- AND (&&)
- OR (||)
- NOT (!)
- LPAREN (()
- RPAREN ())
- LBRACKET ([)
- RBRACKET (])
- COMMA (,)
- SEMICOLON (;)

---

## 4. Símbolo inicial

El símbolo inicial de la gramática es: 

PROGRAM

---

## 5. Producciones

### Programa

PROGRAM → FUNCTION_LIST

### Lista de funciones

FUNCTION_LIST → FUNCTION FUNCTION_LIST  
FUNCTION_LIST → ε

### Función

FUNCTION → IDENTIFIER LPAREN PARAM_LIST_OPC RPAREN BLOCK

### Parámetros

PARAM_LIST_OPC → PARAM_LIST  
PARAM_LIST_OPC → ε

PARAM_LIST → IDENTIFIER PARAM_LIST'

PARAM_LIST' → COMMA IDENTIFIER PARAM_LIST'  
PARAM_LIST' → ε

### Bloque

BLOCK → LBRACKET STATEMENT_LIST RBRACKET

### Lista de sentencias

STATEMENT_LIST → STATEMENT STATEMENT_LIST  
STATEMENT_LIST → ε

### sentencias

STATEMENT → DECLARATION  
STATEMENT → ARRAY_DECLARATION  
STATEMENT → ASSIGNMENT  
STATEMENT → IF_STATEMENT  
STATEMENT → WHILE_STATEMENT  
STATEMENT → PRINT_STATEMENT  
STATEMENT → RETURN_STATEMENT

### Declaración

DECLARATION → LET IDENTIFIER ASSIGN EXPRESSION SEMICOLON

### Declaración de arreglo

ARRAY_DECLARATION → ARRAY IDENTIFIER ASSIGN NEW LPAREN EXPRESSION RPAREN SEMICOLON

### Asignación

ASSIGNMENT → IDENTIFIER ASSIGNMENT_TAIL

ASSIGNMENT_TAIL → ASSIGN EXPRESSION SEMICOLON  
ASSIGNMENT_TAIL → PLUS_ASSIGN EXPRESSION SEMICOLON  
ASSIGNMENT_TAIL → MINUS_ASSIGN EXPRESSION SEMICOLON  
ASSIGNMENT_TAIL → MUL_ASSIGN EXPRESSION SEMICOLON  
ASSIGNMENT_TAIL → DIV_ASSIGN EXPRESSION SEMICOLON  
ASSIGNMENT_TAIL → LBRACKET EXPRESSION RBRACKET ASSIGN EXPRESSION SEMICOLON  
ASSIGNMENT_TAIL → LBRACKET EXPRESSION RBRACKET PLUS_ASSIGN EXPRESSION SEMICOLON

### Estructura condicional `if`

IF_STATEMENT → IF LPAREN EXPRESSION RPAREN BLOCK ELSE_OPC

ELSE_OPC → ELSE BLOCK  
ELSE_OPC → ε

### Ciclo `while`

WHILE_STATEMENT → WHILE LPAREN EXPRESSION RPAREN BLOCK

### Instrucción `print`

PRINT_STATEMENT → PRINT LPAREN EXPRESSION RPAREN SEMICOLON

### Instrucción `return`

RETURN_STATEMENT → RETURN EXPRESSION SEMICOLON

---

## 6. Expresiones

EXPRESSION → LOGIC_OR

### OR lógico

LOGIC_OR → LOGIC_AND LOGIC_OR'

LOGIC_OR' → OR LOGIC_AND LOGIC_OR'  
LOGIC_OR' → ε

### AND lógico

LOGIC_AND → EQUALITY LOGIC_AND'

LOGIC_AND' → AND EQUALITY LOGIC_AND'  
LOGIC_AND' → ε

### Igualdad

EQUALITY → RELATIONAL EQUALITY'

EQUALITY' → EQ RELATIONAL EQUALITY'  
EQUALITY' → NE RELATIONAL EQUALITY'  
EQUALITY' → ε

### Relacionales

RELATIONAL → TERM RELATIONAL'

RELATIONAL' → LT TERM RELATIONAL'  
RELATIONAL' → GT TERM RELATIONAL'  
RELATIONAL' → LE TERM RELATIONAL'  
RELATIONAL' → GE TERM RELATIONAL'  
RELATIONAL' → ε

### Suma y resta

TERM → FACTOR TERM'

TERM' → PLUS FACTOR TERM'  
TERM' → MINUS FACTOR TERM'  
TERM' → ε

### Multiplicación y división

FACTOR → UNARY FACTOR'

FACTOR' → STAR UNARY FACTOR'  
FACTOR' → SLASH UNARY FACTOR'  
FACTOR' → ε

### Unarios

UNARY → NOT UNARY  
UNARY → MINUS UNARY  
UNARY → PRIMARY

### Primary

PRIMARY → NUMBER  
PRIMARY → CHAR  
PRIMARY → STRING  
PRIMARY → IDENTIFIER PRIMARY_TAIL  
PRIMARY → LPAREN EXPRESSION RPAREN

### Llamadas a función y acceso a arreglos

PRIMARY_TAIL → LPAREN ARG_LIST_OPC RPAREN  
PRIMARY_TAIL → LBRACKET EXPRESSION RBRACKET  
PRIMARY_TAIL → ε

### Argumentos

ARG_LIST_OPC → ARG_LIST  
ARG_LIST_OPC → ε

ARG_LIST → EXPRESSION ARG_LIST'

ARG_LIST' → COMMA EXPRESSION ARG_LIST'  
ARG_LIST' → ε

---

## 7. Notas finales

Esta gramática fue organizada para ser utilizada por un analizador sintáctico LL(1).

Se corrigieron los siguientes puntos:

1. Se definió `PROGRAM` como una lista de funciones.
2. Se agregó `BLOCK`, ya que el lenguaje KAJ usa bloques con corchetes.
3. Se agregó declaración de arreglos con `ARRAY` y `NEW`.
4. Se agregó acceso a arreglos con `IDENTIFIER [ EXPRESSION ]`.
5. Se agregó llamada a función con `IDENTIFIER ( ARG_LIST_OPC )`.
6. Se agregó negación lógica con `NOT`.
7. Se agregó signo menos unario con `MINUS UNARY`.
8. Se diferenciaron terminales y no terminales mediante una convención explícita.

La precedencia de operadores queda en este orden:

1. OR lógico
2. AND lógico
3. Igualdad
4. Relacionales
5. Suma y resta
6. Multiplicación y división
7. Operadores unarios
8. Valores primarios