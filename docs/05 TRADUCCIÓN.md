# Reglas de traducción del lenguaje KAJ

## 1. Introducción

El proceso de traducción en el lenguaje KAJ consiste en recorrer el Árbol de Sintaxis Abstracta (AST), previamente validado sintáctica y semánticamente, para generar código en lenguaje ensamblador dirigido a una máquina virtual basada en pila (StackVM).

La traducción se implementa utilizando el patrón Visitor, permitiendo recorrer cada nodo del AST y generar las instrucciones correspondientes de forma modular y extensible.

---

## 2. Modelo de ejecución: StackVM

La StackVM es una máquina virtual basada en pila con las siguientes características:

- Todas las operaciones se realizan sobre una pila.
- Los operandos se colocan en la pila mediante instrucciones PUSH o LOAD.
- Las operaciones consumen valores de la pila y colocan el resultado nuevamente en ella.
- No existen registros; todo el cálculo se realiza mediante la pila.

---

## 3. Conjunto de instrucciones utilizadas

El código generado utiliza las siguientes instrucciones:

### Manejo de valores

```text
PUSH c        ; Inserta constante en la pila
LOAD x        ; Carga el valor de una variable
STORE x       ; Almacena el valor en una variable
```

### Operaciones aritméticas

```text
ADD, SUB, MUL, DIV
```

### Operaciones relacionales

```text
LT, GT, LE, GE, EQ, NE
```

### Operaciones lógicas

```text
AND, OR, NOT
```

### Control de flujo

```text
JMP L         ; Salto incondicional
JZ L          ; Salta si el valor es 0 (falso)
LABEL L       ; Define una etiqueta
```

### Funciones

```text
CALL f        ; Llamada a función
RET           ; Retorno de función
```

### Entrada/Salida

```text
PRINT         ; Imprime valor
```

### Arreglos 

```text
LOAD_IND      ; Carga valor indirecto
STORE_IND     ; Almacena valor indirecto
```

---

## 4. Traducción por nodos del AST
## 4.1 Expresiones

### Arreglos 
Código KAJ
```text
5
```
Traducción
```text
PUSH 5
```
### IdentExpr
Código KAJ
```text
.x
```
Traducción
```text
LOAD .x
```

### BinaryExpr
Código KAJ
```text
.x + 5
```
Traducción
```text
LOAD .x
PUSH 5
ADD
```

El proceso de traducción es:

1. Evaluar operando izquierdo
2. Evaluar operando derecho
3. Aplicar operación

### UnaryExpr
Negación aritmética
Código KAJ
```text
-.x
```
Traducción
```text
LOAD .x
NEG
```
Negación lógica
Código KAJ
```text
!cond
```
Traducción
```text
LOAD cond
NOT
```
---

## 4.2 Declaración de variables

### VarDecl
Código KAJ
```text
@let .x = 5;
```
Traducción
```text
PUSH 5
STORE .x
```
---

## 4.3 Asignaciones
Código KAJ
```text
.x = .y + 1;
```
Traducción
```text
LOAD .y
PUSH 1
ADD
STORE .x
```
---

## 4.4 If Statement
Código KAJ
```text
@if (.x < 5) [
    ...
] @else [
    ...
]
```
Traducción
```text
LOAD .x
PUSH 5
LT
JZ L_else_1

; THEN
...

JMP L_end_1

LABEL L_else_1
; ELSE
...

LABEL L_end_1
```
Las etiquetas (L_else_1, L_end_1) se generan automáticamente usando contadores para evitar colisiones.

---

## 4.5 While Statement
Código KAJ

```text
@while (.x < 5) [
    ...
]
```

Traducción

```text
LABEL L_start_1

LOAD .x
PUSH 5
LT
JZ L_end_1

; cuerpo
...

JMP L_start_1

LABEL L_end_1
```

---

## 4.6 Print
Código KAJ
```text
@print(.x);
```
Traducción
```text
LOAD .x
PRINT
```
---

## 4.7 Return
Código KAJ
```text
@return .x;
```
Traducción
```text
LOAD .x
RET
```
---

## 4.8 Funciones

### Declaración
Código KAJ
```text
.sum(.a, .b) [
    ...
]
```
Traducción
```text
LABEL sum

; cuerpo

RET
```
---

### Llamada
Código KAJ
```text
.sum(5, 3)
```
Traducción
```text
PUSH 5
PUSH 3
CALL sum
```
---

## 4.9 Arreglos

### Acceso
Código KAJ
```text
.A[.i]
```
Traducción
```text
LOAD .A
LOAD .i
ADD
LOAD_IND
```
---

### Asignación
Código KAJ
```text
.A[.i] = 5;
```
Traducción
```text
LOAD .A
LOAD .i
ADD
PUSH 5
STORE_IND
```
---

## 5. Ejemplo completo

## Código KAJ

Cada variable posee una posición en memoria.

Ejemplo:

```kaj
.main() [
    @let .x = 5;
    @if (.x < 10) [
        @print(.x);
    ]
]
```

Código StackVM generado

```text
LABEL main

PUSH 5
STORE .x

LOAD .x
PUSH 10
LT
JZ L_else_1

LOAD .x
PRINT

JMP L_end_1

LABEL L_else_1

LABEL L_end_1

RET
```

---

## 6. Consideraciones de implementación

- La traducción se realiza mediante el patrón Visitor, recorriendo el AST.

- Cada nodo del AST implementa un método accept.

- El generador de código implementa un visitante que produce instrucciones StackVM.

- Se utiliza un contador interno para generar etiquetas únicas.
---
## 7. Conclusión

El proceso de traducción permite transformar programas escritos en KAJ en código ejecutable en StackVM, respetando las reglas semánticas del lenguaje. La utilización del patrón Visitor facilita la extensibilidad y mantenimiento del compilador.
---
## 8. Implementación del generador de código

El generador de código se implementa en el paquete `edu.ic5701.gen` utilizando el patrón Visitor.

Se define una clase `CodeGenerator` que implementa un visitante sobre el AST, donde cada método `visit` genera las instrucciones correspondientes de StackVM.

Ejemplo:

- `visit(NumberExpr)` genera `PUSH`
- `visit(IdentExpr)` genera `LOAD`
- `visit(BinaryExpr)` genera instrucciones como `ADD`, `SUB`, etc.
- `visit(IfStmt)` genera etiquetas y saltos (`JZ`, `JMP`)
- `visit(WhileStmt)` genera ciclos con `LABEL`

Además, se utiliza un contador interno para generar etiquetas únicas (`L1`, `L2`, etc.).

El resultado final es una lista de instrucciones que se escribe en un archivo `.out`.


