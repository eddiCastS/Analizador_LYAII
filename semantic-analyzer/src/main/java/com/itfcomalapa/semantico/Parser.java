package com.itfcomalapa.semantico;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Analizador sintáctico-semántico descendente que además:
 *  - 1.2 Registra acciones semánticas conforme reconoce cada símbolo.
 *  - 1.3 Comprueba tipos en cada operación de la expresión.
 *  - 1.4 Usa una pila semántica explícita (operandos y operadores) para construir
 *        el árbol de expresión, con bitácora de cada push/pop.
 *  - 1.5 Genera un esquema de traducción (código de tres direcciones).
 *  - 1.6 Llena la tabla de símbolos / tabla de direcciones.
 *  - 1.7 Reporta errores semánticos (redeclaración, variable no declarada,
 *        tipos incompatibles, división entre cero) sin detener todo el análisis.
 */
public class Parser {

    private final List<Token> tokens;
    private int pos = 0;

    private final SymbolTable symbolTable = new SymbolTable();
    private final List<String> semanticActions = new ArrayList<>();
    private final List<String> semanticStackLog = new ArrayList<>();
    private final List<String> translation = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    private Node lastExpressionTree = null;
    private int tempCounter = 1;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // ---------- API pública de resultados ----------
    public SymbolTable getSymbolTable() { return symbolTable; }
    public List<String> getSemanticActions() { return semanticActions; }
    public List<String> getSemanticStackLog() { return semanticStackLog; }
    public List<String> getTranslation() { return translation; }
    public List<String> getErrors() { return errors; }
    public Node getLastExpressionTree() { return lastExpressionTree; }

    // ---------- Utilidades de flujo de tokens ----------
    private Token peek() { return tokens.get(pos); }
    private Token advance() { return tokens.get(pos++); }
    private boolean check(TokenType t) { return peek().type == t; }

    private Token expect(TokenType t, String mensaje) {
        if (check(t)) return advance();
        throw new SemanticException(mensaje + " (se encontró '" + peek().lexeme + "')", peek().line);
    }

    // ---------- Punto de entrada ----------
    public void parseProgram() {
        while (!check(TokenType.EOF)) {
            try {
                parseDeclaration();
            } catch (SemanticException e) {
                errors.add("Línea " + e.line + ": " + e.getMessage());
                recoverToNextStatement();
            }
        }
    }

    /** Ante un error, avanza hasta el próximo ';' (o EOF) para poder seguir analizando. */
    private void recoverToNextStatement() {
        while (!check(TokenType.SEMI) && !check(TokenType.EOF)) advance();
        if (check(TokenType.SEMI)) advance();
    }

    // declaracion -> "int" ID "=" expr ";"
    private void parseDeclaration() {
        Token kw = expect(TokenType.INT_KW, "Se esperaba la palabra reservada 'int'");
        semanticActions.add("Línea " + kw.line + ": token reconocido -> 'int' (palabra reservada, tipo base)");

        Token idTok = expect(TokenType.ID, "Se esperaba un identificador después de 'int'");
        semanticActions.add("Línea " + idTok.line + ": token reconocido -> identificador '" + idTok.lexeme + "'");

        if (symbolTable.isDeclared(idTok.lexeme)) {
            throw new SemanticException("Variable '" + idTok.lexeme + "' ya había sido declarada", idTok.line);
        }

        expect(TokenType.ASSIGN, "Se esperaba '=' después del identificador");
        semanticActions.add("Línea " + idTok.line + ": acción semántica -> inicia evaluación de expresión asignada a '" + idTok.lexeme + "'");

        Node expr = parseExpr();
        lastExpressionTree = expr;

        expect(TokenType.SEMI, "Se esperaba ';' al final de la declaración");

        if ("error".equals(expr.type)) {
            // Ya se reportó el error de fondo (variable no declarada, etc.) al construir la expresión.
            symbolTable.declare(idTok.lexeme, "int", "?", idTok.line, false);
            semanticActions.add("Línea " + idTok.line + ": se registra '" + idTok.lexeme +
                    "' en la tabla de símbolos con valor indeterminado por error previo");
            return;
        }

        String valorMostrado = expr.isLeaf ? expr.label : expr.toInfix();
        SymbolEntry entry = symbolTable.declare(idTok.lexeme, "int", valorMostrado, idTok.line, true);
        semanticActions.add("Línea " + idTok.line + ": acción semántica -> crear entrada en tabla de símbolos '" +
                idTok.lexeme + "' (tipo int, dirección " + entry.address + ")");

        // 1.5 Esquema de traducción / código de tres direcciones
        String rhs = genCode(expr);
        translation.add(idTok.lexeme + " = " + rhs);
    }

    // expr -> term (("+"|"-") term)*   /  term -> factor (("*"|"/") factor)*
    // Implementado con una pila semántica explícita (operandos + operadores),
    // aplicando precedencia de operadores (algoritmo tipo shunting-yard).
    private Node parseExpr() {
        Deque<Node> operands = new ArrayDeque<>();
        Deque<String> operators = new ArrayDeque<>();

        pushOperand(operands, parseFactor());
        applyPendingUnary();

        while (check(TokenType.PLUS) || check(TokenType.MINUS) || check(TokenType.STAR) || check(TokenType.SLASH)) {
            String op = advance().lexeme;
            while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(op)) {
                reduceTop(operands, operators);
            }
            pushOperator(operators, op);
            pushOperand(operands, parseFactor());
        }

        while (!operators.isEmpty()) {
            reduceTop(operands, operators);
        }

        Node result = operands.pop();
        semanticStackLog.add("Expresión resuelta. Pila de operandos final: [ " + result.label + (result.isLeaf ? "" : " (subárbol)") + " ] -> pila vacía");
        return result;
    }

    private void applyPendingUnary() { /* reservado para extensión futura (signos unarios) */ }

    private int precedence(String op) {
        return switch (op) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            default -> 0;
        };
    }

    private void pushOperand(Deque<Node> operands, Node n) {
        operands.push(n);
        semanticStackLog.add("PUSH operando '" + (n.isLeaf ? n.label : n.toInfix()) +
                "' -> pila operandos: " + stackAsString(operands));
    }

    private void pushOperator(Deque<String> operators, String op) {
        operators.push(op);
        semanticStackLog.add("PUSH operador '" + op + "' -> pila operadores: " + operators);
    }

    private void reduceTop(Deque<Node> operands, Deque<String> operators) {
        String op = operators.pop();
        Node right = operands.pop();
        Node left = operands.pop();
        semanticStackLog.add("POP operador '" + op + "' y POP operandos '" +
                (left.isLeaf ? left.label : left.toInfix()) + "', '" + (right.isLeaf ? right.label : right.toInfix()) + "'");

        String resultType = checkTypes(left, right, op);
        Node combined = Node.op(op, left, right, resultType);
        operands.push(combined);
        semanticActions.add("Acción semántica -> se construye subárbol (" + combined.toInfix() +
                ") con tipo resultante: " + resultType);
        semanticStackLog.add("PUSH resultado '(" + combined.toInfix() + ")' -> pila operandos: " + stackAsString(operands));
    }

    private String stackAsString(Deque<Node> operands) {
        StringBuilder sb = new StringBuilder("[ ");
        for (Node n : operands) sb.append(n.isLeaf ? n.label : "(" + n.toInfix() + ")").append(" ");
        sb.append("]");
        return sb.toString();
    }

    // factor -> ID | NUM | "(" expr ")"
    private Node parseFactor() {
        Token t = peek();
        if (check(TokenType.NUM)) {
            advance();
            semanticActions.add("Línea " + t.line + ": token reconocido -> número '" + t.lexeme + "' (tipo int)");
            return Node.leaf(t.lexeme, "int");
        }
        if (check(TokenType.ID)) {
            advance();
            if (!symbolTable.isDeclared(t.lexeme)) {
                errors.add("Línea " + t.line + ": variable '" + t.lexeme + "' no ha sido declarada");
                semanticActions.add("Línea " + t.line + ": ERROR semántico -> variable '" + t.lexeme + "' no declarada");
                return Node.leaf(t.lexeme, "error");
            }
            SymbolEntry entry = symbolTable.get(t.lexeme);
            if (!entry.initialized) {
                errors.add("Línea " + t.line + ": variable '" + t.lexeme + "' se usa sin haber sido inicializada");
            }
            semanticActions.add("Línea " + t.line + ": token reconocido -> identificador '" + t.lexeme +
                    "' (tipo " + entry.type + ")");
            return Node.leaf(t.lexeme, entry.type);
        }
        if (check(TokenType.LPAREN)) {
            advance();
            Node inner = parseExpr();
            expect(TokenType.RPAREN, "Se esperaba ')' para cerrar la expresión");
            return inner;
        }
        throw new SemanticException("Se esperaba un identificador, un número o '('", t.line);
    }

    // ---------- 1.3 Comprobación de tipos ----------
    private String checkTypes(Node left, Node right, String op) {
        if ("error".equals(left.type) || "error".equals(right.type)) {
            return "error";
        }
        if (!left.type.equals("int") || !right.type.equals("int")) {
            errors.add("Tipos incompatibles en la operación '" + left.toInfix() + " " + op + " " + right.toInfix() + "'");
            return "error";
        }
        if (op.equals("/") && right.isLeaf && right.label.matches("\\d+") && right.label.equals("0")) {
            errors.add("División entre cero en la expresión '" + left.toInfix() + " / " + right.toInfix() + "'");
            return "error";
        }
        return "int";
    }

    // ---------- 1.5 Esquema de traducción: código de tres direcciones ----------
    private String genCode(Node n) {
        if (n.isLeaf) return n.label;
        String leftCode = genCode(n.left);
        String rightCode = genCode(n.right);
        String temp = "t" + (tempCounter++);
        translation.add(temp + " = " + leftCode + " " + n.label + " " + rightCode);
        return temp;
    }
}
