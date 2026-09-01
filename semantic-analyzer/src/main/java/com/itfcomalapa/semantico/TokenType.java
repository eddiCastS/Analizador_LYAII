package com.itfcomalapa.semantico;

/**
 * Tipos de token reconocidos por el analizador léxico.
 * Gramática soportada:
 *   programa   -> (declaracion)*
 *   declaracion-> "int" ID "=" expr ";"
 *   expr       -> term (("+" | "-") term)*
 *   term       -> factor (("*" | "/") factor)*
 *   factor     -> ID | NUM | "(" expr ")"
 */
public enum TokenType {
    INT_KW,     // palabra reservada "int"
    ID,         // identificador
    NUM,        // literal numérico entero
    PLUS, MINUS, STAR, SLASH,
    ASSIGN,     // "="
    SEMI,       // ";"
    LPAREN, RPAREN,
    EOF
}
