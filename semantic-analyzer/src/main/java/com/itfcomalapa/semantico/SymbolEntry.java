package com.itfcomalapa.semantico;

/** Una fila de la tabla de símbolos / tabla de direcciones (1.6). */
public class SymbolEntry {
    public final String name;
    public final String type;
    public String value;       // literal, o la expresión infija si el valor no es constante
    public final String address; // dirección simbólica asignada (p.ej. 1000)
    public final int line;
    public boolean initialized;

    public SymbolEntry(String name, String type, String value, String address, int line, boolean initialized) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.address = address;
        this.line = line;
        this.initialized = initialized;
    }
}
