# Semántica operativa del lenguaje KAJ

## 1. Descripción general

La semántica operativa de KAJ describe cómo se ejecutan los programas del lenguaje una vez que han pasado el análisis léxico, sintáctico y la construcción del AST.

La ejecución de un programa KAJ inicia en la función `main`.

---

## 2. Estado de ejecución

Durante la ejecución se mantiene un estado compuesto por:

- una tabla de funciones
- una pila de ambientes
- una memoria para variables
- una memoria para arreglos

Cada ambiente representa un scope del programa.

---

## 3. Funciones

Nodo: `Program(functions)`

Primero se registran todas las funciones del programa.

Después se busca la función `main`.

Si `main` existe, se ejecuta su bloque.

Si `main` no existe, se reporta error semántico.

---

Nodo: `FuncDecl(name, params, body)`

Una función define:

- nombre
- lista de parámetros
- bloque de instrucciones

Cuando una función es llamada:

1. Se evalúan sus argumentos.
2. Se crea un nuevo ambiente.
3. Se asocian parámetros con argumentos.
4. Se ejecuta el bloque de la función.
5. Si aparece `return`, se devuelve su valor.

---

## 4. Bloques

Nodo: `Block(statements)`

Al entrar a un bloque:

1. Se crea un nuevo scope.
2. Se ejecutan sus sentencias en orden.
3. Al salir, se elimina el scope.

---

## 5. Variables

Nodo: `VarDecl(name, init)`

Se evalúa la expresión `init`.

Luego se almacena el valor en el ambiente actual con el nombre `name`.

---

Nodo: `AssignStmt(name, op, value)`

Primero se busca la variable `name`.

Luego se evalúa `value`.

Según el operador:

- `=` reemplaza el valor actual
- `+=` suma al valor actual
- `-=` resta al valor actual
- `*=` multiplica el valor actual
- `/=` divide el valor actual

---

## 6. Arreglos

Nodo: `ArrayDecl(name, size)`

Se evalúa `size`.

El tamaño debe ser entero.

Se crea un arreglo con esa cantidad de posiciones.

---

Nodo: `ArrayAccessExpr(name, index)`

Se evalúa `index`.

El índice debe ser entero.

Se obtiene el valor almacenado en esa posición del arreglo.

---

Nodo: `ArrayAssignStmt(name, index, op, value)`

Se evalúa `index`.

Se evalúa `value`.

Luego se actualiza la posición correspondiente del arreglo.

---

## 7. Condicionales

Nodo: `IfStmt(condition, thenBlock, elseBlock)`

Se evalúa `condition`.

Si el resultado es verdadero, se ejecuta `thenBlock`.

Si el resultado es falso y existe `elseBlock`, se ejecuta `elseBlock`.

La condición debe ser booleana.

---

## 8. Ciclos

Nodo: `WhileStmt(condition, body)`

Se evalúa `condition`.

Mientras sea verdadera, se ejecuta `body`.

La condición debe ser booleana.

---

## 9. Impresión

Nodo: `PrintStmt(expr)`

Se evalúa `expr`.

El resultado se imprime en la salida estándar.

---

## 10. Retorno

Nodo: `ReturnStmt(expr)`

Se evalúa `expr`.

El valor obtenido se devuelve como resultado de la función actual.

---

## 11. Expresiones

Nodo: `NumberExpr(value)`

Produce un valor numérico entero o flotante.

---

Nodo: `StringExpr(value)`

Produce una hilera.

---

Nodo: `CharExpr(value)`

Produce un carácter.

---

Nodo: `IdentExpr(name)`

Busca el valor asociado a `name` en la tabla de símbolos.

---

Nodo: `CallExpr(callee, args)`

Busca la función `callee`.

Evalúa los argumentos.

Ejecuta la función con esos argumentos.

---

Nodo: `BinaryExpr(left, op, right)`

Evalúa `left`.

Evalúa `right`.

Aplica el operador correspondiente:

- aritméticos: `+`, `-`, `*`, `/`
- relacionales: `<`, `>`, `<=`, `>=`
- igualdad: `==`, `!=`
- lógicos: `&&`, `||`

---

Nodo: `UnaryExpr(op, expr)`

Evalúa `expr`.

Si `op` es `-`, cambia el signo del valor numérico.

Si `op` es `!`, niega el valor booleano.

---

## 12. Errores semánticos

El analizador semántico debe reportar errores como:

- uso de variables no declaradas
- uso de funciones no declaradas
- llamada a función con cantidad incorrecta de argumentos
- uso de arreglos no declarados
- índice de arreglo no entero
- condición de `if` o `while` no booleana
- operaciones aritméticas con tipos no numéricos
- operaciones lógicas con tipos no booleanos
- retorno inválido
- función `main` inexistente

---

## 13. Conclusión

La semántica operativa de KAJ define cómo se ejecutan funciones, bloques, variables, arreglos, sentencias y expresiones. Esta especificación sirve como base para implementar el analizador semántico y validar que un programa sea correcto antes de continuar con etapas posteriores del compilador.

