package com.itfcomalapa.semantico;

/** Excepción usada para reportar errores semánticos (o léxicos/sintácticos) con número de línea. */
public class SemanticException extends RuntimeException {
    public final int line;

    public SemanticException(String message, int line) {
        super(message);
        this.line = line;
    }
}
