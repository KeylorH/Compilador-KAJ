# Analizador léxico de KAJ

## 1. Objetivo

El analizador léxico de KAJ reconoce los componentes léxicos del lenguaje y produce una secuencia de tokens que será consumida por el analizador sintáctico.

El lexer debe:
- reconocer lexemas válidos
- aplicar máximo avance
- ignorar espacios en blanco
- reportar error léxico cuando encuentre símbolos inválidos

---

## 2. Categorías léxicas

### Palabras reservadas
- `@let`
- `@if`
- `@else`
- `@while`
- `@print`
- `@return`
- `@array`
- `@new`

### Identificadores
- nombres de funciones
- nombres de variables
- nombres de parámetros
- nombres de arreglos

Todo identificador debe iniciar con punto (`.`), seguido de una letra o guion bajo (`_`). Después puede contener letras, dígitos o guion bajo.

### Literales numéricos
- enteros
- flotantes

### Literales de hilera

Las hileras inician con comilla doble (`"`) y terminan con comilla doble (`"`).

### Operadores de asignación
- `=`
- `+=`
- `-=`
- `*=`
- `/=`

### Operadores aritméticos
- `+`
- `-`
- `*`
- `/`

### Operadores relacionales
- `<`
- `>`
- `<=`
- `>=`
- `==`
- `!=`

### Operadores lógicos
- `&&`
- `||`

### Delimitadores
- `(`
- `)`
- `[`
- `]`
- `,`
- `;`

---

## 3. Expresiones regulares

### Identificadores
```regex
[a-zA-Z_][a-zA-Z0-9_]*
```
### Enteros
```regex
[0-9]+
```
### Flotantes
```regex
[0-9]+\.[0-9]+
```
### Números generales
```regex
[0-9]+(\.[0-9]+)?
```
### Hilera
```regex "[^"]*"
```
### Palabras reservadas
```regex
@let|@if|@else|@while|@print|@return|@array|@new
```
### Operadores de asignación
```regex
=|\+=|-=|\*=|/=
```
### Operadores aritméticos
```regex
\+|-|\*|/
```
### Operadores relacionales
```regex
<=|>=|==|!=|<|>
```
### Operadores lógicos
```regex
&&|\|\|
```
### Delimitadores
```regex
\(|\)|\[|\]|,|;
```
### Espacios en blanco
```regex
[ \t\n\r]+
```
--- 
## 4. Tokens propuestos

* IDENTIFIER
* NUMBER
* CHAR
* LET
* IF
* ELSE
* WHILE
* PRINT
* RETURN
* ARRAY
* NEW
* ASSIGN
* PLUS_ASSIGN
* MINUS_ASSIGN
* MUL_ASSIGN
* DIV_ASSIGN
* PLUS
* MINUS
* STAR
* SLASH
* LT
* GT
* LE
* GE
* EQ
* NE
* AND
* OR
* LPAREN
* RPAREN
* LBRACKET
* RBRACKET
* COMMA
* SEMICOLON
* EOF

---

## 5. Regla de máximo avance
El lexer debe reconocer siempre el lexema más largo posible.

### Ejemplos

* `<=` se reconoce como un solo token
* `>=` se reconoce como un solo token
* `==` se reconoce como un solo token
* `!=` se reconoce como un solo token
* `+=` se reconoce como un solo token
* `.variable1` se reconoce como un solo token `IDENTIFIER`
* `"radar"` se reconoce como un solo token `CHAR`


---

## 6. Prioridad de reconocimiento
Cuando varias expresiones regulares compiten, se recomienda esta prioridad:

1. palabras reservadas
2. identificadores iniciados con `.`
3. hileras encerradas entre comillas dobles
4. números
5. operadores dobles (`<=`, `>=`, `==`, `!=`, `+=`, `-=`, `*=`, `/=`, `&&`, `||`)
6. operadores simples
7. delimitadores
8. espacios en blanco

---

## 7. Diagrama de transiciones
A continuación se describen los diagramas de transición utilizados en el analizador léxico del lenguaje KAJ. Cada diagrama representa un autómata finito determinista (DFA) que reconoce una categoría léxica específica.
### 7.1 Identificadores

Este diagrama reconoce identificadores del lenguaje.

Reglas:
- deben iniciar con punto (`.`)
- después del punto debe venir una letra o guion bajo (`_`)
- luego pueden contener letras, dígitos o guion bajo

Transiciones:

- Estado inicial `q0`
- `q0 → q1` con `.`
- `q1 → q2` con letra o `_`
- `q2 → q2` con letra, dígito o `_`
- `q2 → aceptación` con cualquier otro símbolo

Token generado:
- `IDENTIFIER`

Ejemplo:
`.x`
`.var1`
`._temp`

### 7.2 Números enteros y flotantes

Este autómata reconoce números enteros y flotantes.

Reglas:
- Un entero es una secuencia de dígitos
- Un flotante incluye un punto decimal

Transiciones:

- `q0 → q1` con dígito
- `q1 → q1` con dígito
- `q1 → q2` con `.`
- `q1 → aceptación` → número entero
- `q2 → q3` con dígito
- `q3 → q3` con dígito
- `q3 → aceptación` → número flotante

Tokens generados:
- `NUMBER (int)`
- `NUMBER (float)`

Ejemplos:
`10`
`25`
`3.14`
`0.5`

### 7.3 Hileras

Este autómata reconoce literales encerrados entre comillas dobles.

Reglas:

- una hilera debe iniciar con `"`
- una hilera debe terminar con `"`
- el contenido puede estar vacío
- el contenido puede incluir letras, dígitos, espacios y símbolos, excepto una comilla doble no escapada

Transiciones:

- `q0 → q1` con `"`
- `q1 → q1` con cualquier carácter distinto de `"`
- `q1 → q2` con `"`
- `q2 → aceptación`

Token generado:

- `CHAR`

Ejemplo:
`"a"`
`"radar"`
`"hola mundo"`

### 7.4 Palabras reservadas con @
Este diagrama reconoce palabras reservadas del lenguaje.

Reglas:
- Todas las palabras reservadas comienzan con `@`
- Luego siguen letras

Transiciones:

- `q0 → q1` con `@`
- `q1 → q2` con letra
- `q2 → q2` con letra
- `q2 → aceptación` y se valida si es palabra reservada

Proceso adicional:
- Se compara el lexema contra:
    - `@let`, `@if`, `@else`, `@while`, `@print`, `@return`, `@array`, `@new`

Tokens generados:
- `LET`, `IF`, `ELSE`, etc.

Ejemplo:
`@let`
`@if`
`@while`

### 7.5 Asignación e igualdad
Este diagrama reconoce operadores de asignación y comparación.

Transiciones:

- `q0 → q1` con `=`
- `q1 → aceptación` → `=`
- `q1 → aceptación` con `=` → `==`

- `q0 → q2` con `+`
- `q2 → aceptación` con `=` → `+=`

- `q0 → q3` con `-`
- `q3 → aceptación` con `=` → `-=`

- `q0 → q4` con `*`
- `q4 → aceptación` con `=` → `*=`

- `q0 → q5` con `/`
- `q5 → aceptación` con `=` → `/=`
- `q5 → aceptación` → `/`

Tokens generados:
- `ASSIGN (=)`
- `EQ (==)`
- `PLUS_ASSIGN (+=)`
- `MINUS_ASSIGN (-=)`
- `MUL_ASSIGN (*=)`
- `DIV_ASSIGN (/=)`
- `SLASH (/)`

Ejemplo:
`=`
`+=`
`-=`
`*=`
`/=`

### 7.6 Relacionales
Este autómata reconoce operadores de comparación.

Transiciones:

- `q0 → q1` con `<`
- `q1 → aceptación` → `<`
- `q1 → aceptación` con `=` → `<=`

- `q0 → q2` con `>`
- `q2 → aceptación` → `>`
- `q2 → aceptación` con `=` → `>=`

- `q0 → q3` con `!`
- `q3 → aceptación` con `=` → `!=`

Tokens generados:
- `LT (<)`
- `GT (>)`
- `LE (<=)`
- `GE (>=)`
- `NE (!=)`

Ejemplo:
`<`
`<=`
`>`
`>=`
`!=`

### 7.7 Operadores lógicos
Este diagrama reconoce operadores booleanos.

Reglas:
- siempre se componen de dos caracteres

Transiciones:

- `q0 → q1` con `&`
- `q1 → aceptación` con `&` → `&&`

- `q0 → q2` con `|`
- `q2 → aceptación` con `|` → `||`

Tokens generados:
- `AND (&&)`
- `OR (||)`

Ejemplo:
`&&`
`||`

### 7.8 Delimitadores
Este autómata reconoce símbolos individuales del lenguaje.

Transiciones directas desde el estado inicial:

- `(` → `LPAREN`
- `)` → `RPAREN`
- `[` → `LBRACKET`
- `]` → `RBRACKET`
- `,` → `COMMA`
- `;` → `SEMICOLON`

Tokens generados:
- `LPAREN`
- `RPAREN`
- `LBRACKET`
- `RBRACKET`
- `COMMA`
- `SEMICOLON`

Ejemplo:
`( )`
`[ ]`
`, ;`

### 7.9 Observaciones generales

- Algunos tokens requieren continuar la transición para decidir si se acepta el lexema más largo posible, por ejemplo `=` frente a `==`.
- Los lexemas que inician con `@` se reconocen primero como una secuencia válida y luego se validan contra la tabla de palabras reservadas.
- Si un lexema inicia con `@` pero no coincide con una palabra reservada, se reporta error léxico.
- Los identificadores deben iniciar con `.`.
- Un punto aislado (`.`) o un punto seguido de un carácter inválido produce error léxico.
- Las hileras deben cerrar con comilla doble.
- Si se llega al final de archivo sin encontrar la comilla de cierre de una hilera, se reporta error léxico.
- En el estado inicial, la clase `WHITESPACE` reinicia el scanner sin emitir token.

---

## 8. Clases de carácter

Para implementar la tabla de transiciones, los caracteres de entrada se agrupan en clases:

- LETTER → [a-zA-Z]
- DIGIT → [0-9]
- UNDERSCORE → _
- AT → @
- DOT → .
- QUOTE → `"`
- EQUAL → =
- PLUS → +
- MINUS → -
- STAR → *
- SLASH → /
- LT → <
- GT → >
- BANG → !
- AMP → &
- PIPE → |
- LPAREN → (
- RPAREN → )
- LBRACKET → [
- RBRACKET → ]
- COMMA → ,
- SEMICOLON → ;
- WHITESPACE → espacios, tabulaciones, saltos de línea
- OTHER → cualquier otro símbolo

---

## 9. Algoritmo del scanner basado en tabla

El scanner se implementa recorriendo la tabla de transiciones.

Pseudocódigo:

```text
func nextToken():
    estado = q0
    lexema = ""
    ultimoAceptado = null

    mientras haya caracteres:
        c = ver siguiente carácter
        clase = clasificar(c)

        si estado == q0 y clase == WHITESPACE:
            consumir c
            continuar

        si estado == q0 y c == ".":
            consumir c
            lexema = lexema + c
            estado = IDENTIFIER_START
            continuar

        si estado == IDENTIFIER_START:
            si clase == LETTER o clase == UNDERSCORE:
                consumir c
                lexema = lexema + c
                estado = IDENTIFIER_CONTENT
                ultimoAceptado = (estado, lexema)
                continuar
            sino:
                reportar error léxico:
                    "Identificador inválido. Después del punto debe venir una letra o guion bajo."
                devolver ERROR

        si estado == IDENTIFIER_CONTENT:
            si clase == LETTER o clase == DIGIT o clase == UNDERSCORE:
                consumir c
                lexema = lexema + c
                ultimoAceptado = (estado, lexema)
                continuar
            sino:
                devolver token IDENTIFIER con lexema

        siguiente = tabla[estado][clase]

        si siguiente es ERROR:
            si estado == CHAR_START o estado == CHAR_CONTENT:
                reportar error léxico:
                    "Literal char inválido. Falta la comilla de cierre."
                devolver ERROR

            si ultimoAceptado existe:
                devolver token asociado a ultimoAceptado

            reportar error léxico
            consumir c
            devolver ERROR

        consumir c
        lexema = lexema + c
        estado = siguiente

        si estado es de aceptación:
            ultimoAceptado = (estado, lexema)

        si estado == CHAR_END:
            devolver token CHAR con lexema

    si estado == IDENTIFIER_START:
        reportar error léxico:
            "Identificador inválido. Después del punto debe venir una letra o guion bajo."
        devolver ERROR

    si estado == IDENTIFIER_CONTENT:
        devolver token IDENTIFIER con lexema

    si estado == CHAR_START o estado == CHAR_CONTENT:
        reportar error léxico:
            "Literal char inválido. Falta la comilla de cierre."
        devolver ERROR

    si ultimoAceptado existe:
        devolver token asociado a ultimoAceptado

    devolver EOF
```

## 10. Estrategia de implementación

El analizador léxico de KAJ no se implementa mediante funciones separadas por tipo de token ni mediante estructuras condicionales extensas.

En su lugar, se implementa como un autómata finito determinista representado mediante una tabla de transiciones.

El algoritmo del scanner consiste en recorrer esta tabla carácter por carácter hasta alcanzar un estado de aceptación.

### Notas:
Cuando una transición no es válida, el scanner devuelve el último token aceptado sin consumir caracteres que pertenezcan al siguiente token.