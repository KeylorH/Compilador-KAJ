# Especificación informal del lenguaje KAJ

## 1. Descripción general

KAJ es un lenguaje de programación imperativo y estructurado diseñado para la implementación de un compilador. Soporta funciones, variables, arreglos, condicionales, ciclos, expresiones, recursión y literales de caracteres.

El lenguaje es Turing-completo porque incluye:
- memoria indexada (arreglos)
- ciclos (@while)
- condicionales (@if, @else)
- recursión

---

## 2. Forma del programa

Un programa está compuesto por funciones. Todos los identificadores del lenguaje inician con punto (`.`).

```kaj
.main() [
    @let .x = 10;
    @print(.x);
]
```
## 3. Funciones

Las funciones se escriben con un identificador, una lista opcional de parámetros y un bloque delimitado por corchetes.

Los nombres de funciones y parámetros son identificadores, por lo que deben iniciar con `.`.

```kaj
.nombre(.param1, .param2, ...) [
    ...
]
```
Ejemplo:

```kaj
.fibo(.n) [
    @if (.n <= 1) [
        @return .n;
    ]
    @else [
        @return .fibo(.n - 1) + .fibo(.n - 2);
    ]
]
```
## 4. Bloques

Los bloques en KAJ se delimitan con:

```kaj
[
]
```

Ejemplo:

```kaj
@if (.x > 0) [
    @print(.x);
]
```
## 5. Variables

Las variables se declaran con @let.

Ejemplos:

```kaj
@let .i = 0;
@let .x = 3.14;
```
KAJ soporta números enteros y flotantes.

## 6. Arreglos
KAJ permite arreglos dinámicos mediante @array y @new.

Ejemplo:

```kaj
@array .A = @new(10);
.A[0] = 5;
@print(.A[0]);
```
También se permite acceso y actualización por índice:

```kaj
.A[.i] = .A[.i] + 1;
```

## 7. sentencias del lenguaje
Declaración de variable
```kaj
@let .x = 5;
```

Declaración de arreglo
```kaj
@array .A = @new(20);
```

Asignación
```kaj
.x = 10;
.x += 1;
.x += 1;
.x -= 1;
.x *= 2;
.x /= 2;
```
Asignación sobre arreglos
```kaj
.A[.i] = 7;
.A[.i] += 1;
```
Condicional
```kaj
@if (.x > 0) [
    @print(.x);
]
@else [
    @print(0);
]
```

Ciclo
```kaj
@while (.i < 10) [
    .i += 1;
]
```
Impresión
```kaj
@print(.x);
```
Retorno
```kaj
@return .x;
```
## 8. Expresiones
KAJ soporta:

Operadores aritméticos
* `+`
* `-`
* `*`
* `/`

Operadores relacionales
* `<`
*  `>`
* `<=`
* `>=`
* `==`
* `!=`

Operadores lógicos
* &&
* ||

Agrupación
* paréntesis ()

## 9. Tipos soportados
Actualmente KAJ soporta:

* enteros
* flotantes
* arreglos
* valores booleanos producidos por expresiones relacionales y lógicas

No se definen oficialmente hileras en esta versión del lenguaje.

## 10. Punto de entrada
El programa se ejecuta a partir de la función main().

Ejemplo:
```kaj
.main() [
    @print(1);
]
```
## 11. Árbol de sintaxis abstracta (AST)
El compilador de KAJ construye un AST con nodos para:

* Program
* FuncDecl
* Param
* Block
* VarDecl
* AssignStmt
* ArrayDecl
* ArrayAssignStmt
* IfStmt
* WhileStmt
* PrintStmt
* ReturnStmt
* NumberExpr
* IdentExpr
* CallExpr
* ArrayAccessExpr
* AddExpr
* SubExpr
* MulExpr
* DivExpr
* LessExpr
* GreaterExpr
* LessEqualExpr
* GreaterEqualExpr
* EqualExpr
* NotEqualExpr
* AndExpr
* OrExpr

## 12. Ejemplo del lenguaje
```kaj
.fibo(.n) [
    @if (.n <= 1) [
        @return .n;
    ]
    @else [
        @return .fibo(.n - 1) + .fibo(.n - 2);
    ]
]

.main() [
    @let .i = 0;
    @while (.i < 10) [
        @print(.fibo(.i));
        .i += 1;
    ]
]
```

## 13. Programas de muestra
El compilador debe poder procesar al menos estos programas de ejemplo:

* samples/factorial.kaj
* samples/burbuja.kaj
* samples/raízc.kaj
* samples/palíndrome.kaj


# `samples/factorial.kaj`

```kaj
.factorial(.n) [
    @if (.n <= 1) [
        @return 1;
    ]
    @else [
        @return .n * .factorial(.n - 1);
    ]
]

.main() [
    @print(.factorial(5));
]
```

# `samples/burbuja.kaj`
```kaj
.burbuja() [
    @array .A = @new(5);

    .A[0] = 5;
    .A[1] = 1;
    .A[2] = 4;
    .A[3] = 2;
    .A[4] = 3;

    @let .i = 0;
    @let .j = 0;
    @let .temp = 0;

    @while (.i < 5) [
        .j = 0;
        @while (.j < 4) [
            @if (.A[.j] > .A[.j + 1]) [
                .temp = .A[.j];
                .A[.j] = .A[.j + 1];
                .A[.j + 1] = .temp;
            ]
            .j += 1;
        ]
        .i += 1;
    ]

    .i = 0;
    @while (.i < 5) [
        @print(.A[.i]);
        .i += 1;
    ]
]

.main() [
    .burbuja();
]
```

# `samples/raízc.kaj`
```kaj
.abs(.x) [
    @if (.x < 0.0) [
        @return 0.0 - .x;
    ]
    @else [
        @return .x;
    ]
]

.raizc(.x) [
    @let .r = .x;
    @let .prev = 0.0;
    @let .diff = 1.0;

    @while (.diff > 0.0001) [
        .prev = .r;
        .r = (.r + .x / .r) / 2.0;
        .diff = .abs(.r - .prev);
    ]

    @return .r;
]

.main() [
    @print(.raizc(25.0));
]
```

# `samples/burbuja.kaj`
```kaj
.esPalindromo(.palabra, .longitud) [
    @let .izquierda = 0;
    @let .derecha = .longitud - 1;
    @let .resultado = 1;

    @while (.izquierda < .derecha) [
        @if (.palabra[.izquierda] != .palabra[.derecha]) [
            .resultado = 0;
        ]

        .izquierda += 1;
        .derecha -= 1;
    ]

    @return .resultado;
]

.main() [
    @let .palabra = "radar";

    @if (.esPalindromo(.palabra, 5) == 1) [
        @print(1);
    ]
    @else [
        @print(0);
    ]
]
```