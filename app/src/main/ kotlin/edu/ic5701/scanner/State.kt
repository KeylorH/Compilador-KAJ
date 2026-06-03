package edu.ic5701.scanner

enum class State {
    Q0,

    IDENTIFIER_START,
    IDENTIFIER,

    NUMBER_INT,
    NUMBER_DOT,
    NUMBER_FLOAT,

    CHAR_START,
    CHAR_CONTENT,
    CHAR_END,

    RESERVED_AT,
    RESERVED_WORD,

    ASSIGN,
    PLUS,
    MINUS,
    STAR,
    SLASH,

    LT,
    GT,
    BANG,
    AMP,
    PIPE,

    LPAREN,
    RPAREN,
    LBRACKET,
    RBRACKET,
    COMMA,
    SEMICOLON,

    ERROR
}