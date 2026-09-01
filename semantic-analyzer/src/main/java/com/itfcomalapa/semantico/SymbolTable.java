package com.itfcomalapa.semantico;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tabla de símbolos y tabla de direcciones (1.6).
 * Cada variable declarada recibe una dirección simbólica consecutiva,
 * empezando en 1000 y avanzando de 4 en 4 bytes (tamaño de un int).
 */
public class SymbolTable {
    private final Map<String, SymbolEntry> table = new LinkedHashMap<>();
    private int nextAddress = 1000;
    private static final int WORD_SIZE = 4;

    public boolean isDeclared(String name) {
        return table.containsKey(name);
    }

    public SymbolEntry get(String name) {
        return table.get(name);
    }

    public SymbolEntry declare(String name, String type, String value, int line, boolean initialized) {
        String address = String.valueOf(nextAddress);
        nextAddress += WORD_SIZE;
        SymbolEntry entry = new SymbolEntry(name, type, value, address, line, initialized);
        table.put(name, entry);
        return entry;
    }

    public Map<String, SymbolEntry> entries() {
        return table;
    }

    public void clear() {
        table.clear();
        nextAddress = 1000;
    }
}
