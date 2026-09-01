package com.itfcomalapa.semantico;

/** Representa un token léxico: tipo, lexema y línea de origen. */
public class Token {
    public final TokenType type;
    public final String lexeme;
    public final int line;

    public Token(TokenType type, String lexeme, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    @Override
    public String toString() {
        return type + "('" + lexeme + "')";
    }
}
