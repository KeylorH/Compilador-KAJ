# KAJ Compiler

Compilador desarrollado para el curso **IC-5701 Compiladores e Intérpretes** del Tecnológico de Costa Rica.

KAJ es un lenguaje de programación diseñado desde cero con el objetivo de explorar las principales etapas de construcción de un compilador: análisis léxico, análisis sintáctico, construcción de árboles de sintaxis abstracta, análisis semántico y generación de código.

## Descripción del Proyecto

Este proyecto implementa un compilador completo para el lenguaje **KAJ**, un lenguaje imperativo diseñado con características suficientes para ser computacionalmente expresivo y servir como plataforma de aprendizaje para conceptos fundamentales de compiladores.

El compilador recibe como entrada un programa escrito en KAJ y realiza las siguientes etapas:

1. Análisis léxico (Scanner)
2. Análisis sintáctico (Parser LL(1))
3. Construcción del AST (Abstract Syntax Tree)
4. Análisis semántico
5. Generación de código ejecutable

## Características Implementadas

### Análisis Léxico

* Reconocimiento de palabras reservadas.
* Identificadores.
* Literales numéricos.
* Operadores aritméticos y lógicos.
* Delimitadores y símbolos especiales.
* Reporte de errores léxicos.

### Análisis Sintáctico

* Parser de descenso recursivo.
* Gramática LL(1).
* Detección y reporte de errores sintácticos.
* Construcción automática del AST.

### Análisis Semántico

* Resolución de identificadores.
* Verificación de tipos.
* Validación de declaraciones.
* Reporte de errores semánticos.

### Generación de Código

* Traducción del lenguaje fuente KAJ a lenguaje objetivo.
* Producción automática de archivos ejecutables `.out`.

## Arquitectura

```text
Código Fuente KAJ
        │
        ▼
     Scanner
        │
        ▼
      Parser
        │
        ▼
       AST
        │
        ▼
 Analizador Semántico
        │
        ▼
 Generador de Código
        │
        ▼
 Archivo Ejecutable
```

## Estructura del Proyecto

```text
src/
├── edu.ic5701.scanner
├── edu.ic5701.parser
├── edu.ic5701.ast
├── edu.ic5701.sem
├── edu.ic5701.gen
└── edu.ic5701.Compiler

docs/
├── 01 ESPECIFICACION.md
├── 02 LEXICO.md
├── 03 GRAMATICA.md
├── 04 SEMANTICA.md
└── 05 TRADUCCION.md

samples/
├── factorial
├── burbuja
├── palindromo
└── raizc
```

## Programas de Ejemplo

El compilador incluye varios programas de prueba desarrollados en KAJ:

* Factorial recursivo utilizando pila de llamadas.
* Ordenamiento Burbuja iterativo.
* Verificación de palíndromos.
* Cálculo de raíz cuadrada mediante Newton-Raphson.

## Tecnologías Utilizadas

* Java
* Maven
* IntelliJ IDEA
* PlantUML

## Resultados de Aprendizaje

Durante el desarrollo de este proyecto se aplicaron conceptos de:

* Diseño de lenguajes de programación.
* Teoría de autómatas.
* Expresiones regulares.
* Parsing LL(1).
* Árboles de sintaxis abstracta.
* Análisis semántico.
* Generación de código.
* Construcción de compiladores.

## Autor

**Keylor Herrera Fuentes**

Estudiante de Ingeniería en Computadores
Tecnológico de Costa Rica (TEC)

## Licencia

Proyecto desarrollado con fines académicos para el curso IC-5701 Compiladores e Intérpretes.
