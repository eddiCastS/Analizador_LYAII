package com.itfcomalapa.semantico;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Interfaz gráfica del "Analizador Semántico" (compilador didáctico), en Swing.
 * Disposición: barra de acciones vertical a la izquierda, editor de código fuente
 * arriba, y TODOS los resultados (tabla de símbolos, árbol, resultado, acciones
 * semánticas, pila semántica y traducción) agrupados en un único panel de pestañas
 * a la derecha. Barra de estado inferior con el resumen del último análisis.
 * No requiere dependencias externas: Swing viene incluido en el JDK.
 */
public class Main extends JFrame {

    private final JTextArea sourceArea = new JTextArea();
    private final JTextArea treeArea = new JTextArea();
    private final JTextArea resultArea = new JTextArea();
    private final JTextArea actionsArea = new JTextArea();
    private final JTextArea stackArea = new JTextArea();
    private final JTextArea translationArea = new JTextArea();
    private final JLabel statusBar = new JLabel(" Listo.");

    private final String[] columnNames = {"Nombre", "Tipo", "Valor", "Dirección", "Línea", "Inicializada"};
    private final DefaultTableModel symbolModel = new DefaultTableModel(columnNames, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable symbolTable = new JTable(symbolModel);

    private static final String EJEMPLO_CORRECTO =
            "int a = 10;\n" +
            "int b = 5;\n" +
            "int c = 2;\n" +
            "int d = 4;\n" +
            "int resultado = a + b * c - d;\n";

    private static final String EJEMPLO_CON_ERROR =
            "int a = 10;\n" +
            "int b = 5;\n" +
            "int resultado = a + b * z;\n" +   // 'z' no declarada
            "int resultado = a - b;\n" +        // 'resultado' redeclarada
            "int e = a / 0;\n";                 // división entre cero

    public Main() {
        super("Analizador Semántico — Lenguajes y Autómatas II");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 820);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(0x24, 0x28, 0x33));

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildBody(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        setContentPane(root);

        sourceArea.setText(EJEMPLO_CORRECTO);
        analizar();
    }

    // ---------- Barra lateral izquierda con los botones de acción ----------
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(0x24, 0x28, 0x33));
        sidebar.setBorder(new EmptyBorder(16, 12, 16, 12));
        sidebar.setPreferredSize(new Dimension(190, 0));

        JLabel logo = new JLabel("<html><div style='text-align:center;'>ANÁLISIS<br>SEMÁNTICO</div></html>", SwingConstants.CENTER);
        logo.setForeground(Color.WHITE);
        logo.setFont(logo.getFont().deriveFont(Font.BOLD, 16f));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(new EmptyBorder(0, 0, 24, 0));
        sidebar.add(logo);

        sidebar.add(sidebarButton("▶  Analizar", new Color(0x2E, 0x7D, 0x32), e -> analizar()));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(sidebarButton("✕  Limpiar", new Color(0x6D, 0x6D, 0x6D), e -> limpiar()));
        sidebar.add(Box.createVerticalStrut(30));

        JLabel ejemplosLabel = new JLabel("EJEMPLOS");
        ejemplosLabel.setForeground(Color.LIGHT_GRAY);
        ejemplosLabel.setFont(ejemplosLabel.getFont().deriveFont(Font.BOLD, 11f));
        ejemplosLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(ejemplosLabel);
        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(sidebarButton("Ejemplo correcto", new Color(0x1E, 0x5F, 0x8C),
                e -> { sourceArea.setText(EJEMPLO_CORRECTO); analizar(); }));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(sidebarButton("Ejemplo con error", new Color(0xA6, 0x3C, 0x2E),
                e -> { sourceArea.setText(EJEMPLO_CON_ERROR); analizar(); }));

        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JButton sidebarButton(String text, Color color, java.awt.event.ActionListener listener) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(170, 40));
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.addActionListener(listener);
        return b;
    }

    // ---------- Cuerpo central: editor arriba, pestañas de resultados abajo ----------
    private JSplitPane buildBody() {
        sourceArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        sourceArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel sourcePanel = new JPanel(new BorderLayout());
        JLabel sourceLabel = new JLabel("  Código fuente");
        sourceLabel.setFont(sourceLabel.getFont().deriveFont(Font.BOLD));
        sourceLabel.setBorder(new EmptyBorder(8, 0, 8, 0));
        sourcePanel.add(sourceLabel, BorderLayout.NORTH);
        sourcePanel.add(new JScrollPane(sourceArea), BorderLayout.CENTER);

        JTabbedPane resultTabs = new JTabbedPane(JTabbedPane.TOP);
        symbolTable.setFillsViewportHeight(true);
        symbolTable.setRowHeight(24);
        resultTabs.addTab("Tabla de símbolos", new JScrollPane(symbolTable));
        resultTabs.addTab("Árbol de expresión", scrollable(treeArea));
        resultTabs.addTab("Resultado", scrollable(resultArea));
        resultTabs.addTab("Acciones semánticas", scrollable(actionsArea));
        resultTabs.addTab("Pila semántica", scrollable(stackArea));
        resultTabs.addTab("Traducción", scrollable(translationArea));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sourcePanel, resultTabs);
        split.setResizeWeight(0.35);
        split.setDividerSize(6);
        return split;
    }

    private JScrollPane scrollable(JTextArea area) {
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(8, 8, 8, 8));
        return new JScrollPane(area);
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x1A, 0x1D, 0x24));
        bar.setBorder(new EmptyBorder(6, 12, 6, 12));
        statusBar.setForeground(Color.WHITE);
        bar.add(statusBar, BorderLayout.WEST);
        return bar;
    }

    private void limpiar() {
        sourceArea.setText("");
        symbolModel.setRowCount(0);
        treeArea.setText("");
        resultArea.setText("");
        actionsArea.setText("");
        stackArea.setText("");
        translationArea.setText("");
        statusBar.setText(" Listo.");
    }

    // ---------- Ejecuta el análisis léxico + sintáctico + semántico ----------
    private void analizar() {
        symbolModel.setRowCount(0);
        treeArea.setText("");
        resultArea.setText("");
        actionsArea.setText("");
        stackArea.setText("");
        translationArea.setText("");

        String code = sourceArea.getText();
        Parser parser;
        try {
            List<Token> tokens = new Lexer(code).tokenize();
            parser = new Parser(tokens);
            parser.parseProgram();
        } catch (SemanticException e) {
            resultArea.setText("ANÁLISIS INTERRUMPIDO\n\nLínea " + e.line + ": " + e.getMessage());
            statusBar.setText(" ⚠ Análisis interrumpido en la línea " + e.line);
            return;
        }

        for (SymbolEntry entry : parser.getSymbolTable().entries().values()) {
            symbolModel.addRow(new Object[]{
                    entry.name, entry.type, entry.value, entry.address,
                    entry.line, entry.initialized ? "Sí" : "No"
            });
        }

        Node tree = parser.getLastExpressionTree();
        treeArea.setText(tree != null ? tree.toTreeString() : "(sin expresiones)");

        actionsArea.setText(String.join("\n", parser.getSemanticActions()));
        stackArea.setText(String.join("\n", parser.getSemanticStackLog()));
        translationArea.setText(String.join("\n", parser.getTranslation()));

        List<String> errors = parser.getErrors();
        StringBuilder sb = new StringBuilder();
        if (errors.isEmpty()) {
            sb.append("ANÁLISIS SEMÁNTICO EXITOSO\n\n");
            sb.append("Variables declaradas: ").append(parser.getSymbolTable().entries().size()).append("\n");
            sb.append("No se encontraron errores semánticos.\n");
            statusBar.setText(" ✔ Análisis exitoso — " + parser.getSymbolTable().entries().size() + " variable(s) declarada(s)");
        } else {
            sb.append("ANÁLISIS FALLIDO — ").append(errors.size()).append(" error(es) semántico(s)\n\n");
            for (String err : errors) sb.append(" • ").append(err).append("\n");
            statusBar.setText(" ⚠ " + errors.size() + " error(es) semántico(s) encontrado(s)");
        }
        resultArea.setText(sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            new Main().setVisible(true);
        });
    }
}
