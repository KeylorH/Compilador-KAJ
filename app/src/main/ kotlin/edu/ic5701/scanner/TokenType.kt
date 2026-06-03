package edu.ic5701.scanner

enum class TokenType {

    IDENTIFIER,
    NUMBER,
    CHAR,
    STRING,

    LET,
    IF,
    ELSE,
    WHILE,
    PRINT,
    RETURN,
    ARRAY,
    NEW,

    ASSIGN,
    PLUS_ASSIGN,
    MINUS_ASSIGN,
    MUL_ASSIGN,
    DIV_ASSIGN,

    PLUS,
    MINUS,
    STAR,
    SLASH,

    LT,
    GT,
    LE,
    GE,
    EQ,
    NE,

    AND,
    OR,
    NOT,

    LPAREN,
    RPAREN,
    LBRACKET,
    RBRACKET,
    COMMA,
    SEMICOLON,

    EOF,
    ERROR
}