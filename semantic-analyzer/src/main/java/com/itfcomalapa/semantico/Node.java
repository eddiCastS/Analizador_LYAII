package com.itfcomalapa.semantico;

/**
 * Nodo del árbol de expresión (1.1). Cada nodo lleva su tipo semántico
 * calculado durante el análisis (1.3 comprobación de tipos).
 * Nodos hoja: identificador o número (label = nombre/valor, left=right=null).
 * Nodos internos: operador (label = "+","-","*","/"), left y right son los operandos.
 */
public class Node {
    public final String label;   // texto a mostrar: nombre de variable, número u operador
    public String type;          // tipo semántico resultante: "int" o "error"
    public final Node left;
    public final Node right;
    public final boolean isLeaf;

    private Node(String label, String type, Node left, Node right, boolean isLeaf) {
        this.label = label;
        this.type = type;
        this.left = left;
        this.right = right;
        this.isLeaf = isLeaf;
    }

    public static Node leaf(String label, String type) {
        return new Node(label, type, null, null, true);
    }

    public static Node op(String operator, Node left, Node right, String type) {
        return new Node(operator, type, left, right, false);
    }

    /** Reconstruye la expresión en notación infija, p.ej. "a + b * c - d". */
    public String toInfix() {
        if (isLeaf) return label;
        return left.toInfix() + " " + label + " " + right.toInfix();
    }

    /** Representación en árbol tipo consola, similar a un árbol de expresión clásico. */
    public String toTreeString() {
        StringBuilder sb = new StringBuilder();
        buildTree(sb, "", true);
        return sb.toString();
    }

    private void buildTree(StringBuilder sb, String prefix, boolean isRoot) {
        if (isLeaf) {
            sb.append(prefix).append(label).append(" : ").append(type).append("\n");
        } else {
            sb.append(prefix).append("\u25BC ").append(label).append(" : ").append(type).append("\n");
            String childPrefix = prefix + "    ";
            left.buildTree(sb, childPrefix, false);
            right.buildTree(sb, childPrefix, false);
        }
    }
}
