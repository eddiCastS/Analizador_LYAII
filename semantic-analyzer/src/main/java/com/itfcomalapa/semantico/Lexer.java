package com.itfcomalapa.semantico;

import java.util.ArrayList;
import java.util.List;

/**
 * Analizador léxico simple: convierte el código fuente en una lista de tokens.
 * Reconoce: la palabra reservada "int", identificadores, números enteros,
 * operadores (+ - * /), "=", ";", "(" y ")".
 */
public class Lexer {

    private final String src;
    private int pos = 0;
    private int line = 1;

    public Lexer(String src) {
        this.src = src;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (true) {
            skipWhitespaceAndComments();
            if (pos >= src.length()) {
                tokens.add(new Token(TokenType.EOF, "", line));
                break;
            }
            char c = src.charAt(pos);

            if (Character.isLetter(c) || c == '_') {
                tokens.add(readIdentifierOrKeyword());
            } else if (Character.isDigit(c)) {
                tokens.add(readNumber());
            } else {
                switch (c) {
                    case '+' -> { tokens.add(new Token(TokenType.PLUS, "+", line)); pos++; }
                    case '-' -> { tokens.add(new Token(TokenType.MINUS, "-", line)); pos++; }
                    case '*' -> { tokens.add(new Token(TokenType.STAR, "*", line)); pos++; }
                    case '/' -> { tokens.add(new Token(TokenType.SLASH, "/", line)); pos++; }
                    case '=' -> { tokens.add(new Token(TokenType.ASSIGN, "=", line)); pos++; }
                    case ';' -> { tokens.add(new Token(TokenType.SEMI, ";", line)); pos++; }
                    case '(' -> { tokens.add(new Token(TokenType.LPAREN, "(", line)); pos++; }
                    case ')' -> { tokens.add(new Token(TokenType.RPAREN, ")", line)); pos++; }
                    default -> throw new SemanticException(
                            "Error léxico: carácter no reconocido '" + c + "'", line);
                }
            }
        }
        return tokens;
    }

    private void skipWhitespaceAndComments() {
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c == '\n') {
                line++;
                pos++;
            } else if (Character.isWhitespace(c)) {
                pos++;
            } else if (c == '/' && pos + 1 < src.length() && src.charAt(pos + 1) == '/') {
                while (pos < src.length() && src.charAt(pos) != '\n') pos++;
            } else {
                break;
            }
        }
    }

    private Token readIdentifierOrKeyword() {
        int start = pos;
        while (pos < src.length() && (Character.isLetterOrDigit(src.charAt(pos)) || src.charAt(pos) == '_')) {
            pos++;
        }
        String text = src.substring(start, pos);
        if (text.equals("int")) {
            return new Token(TokenType.INT_KW, text, line);
        }
        return new Token(TokenType.ID, text, line);
    }

    private Token readNumber() {
        int start = pos;
        while (pos < src.length() && Character.isDigit(src.charAt(pos))) {
            pos++;
        }
        return new Token(TokenType.NUM, src.substring(start, pos), line);
    }
}
