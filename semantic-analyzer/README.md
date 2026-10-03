# Analizador Semántico — Compilador didáctico (Java Swing)

Proyecto Maven en Java puro (interfaz con Swing, incluido en el JDK — **sin
dependencias externas que descargar ni módulos que configurar**) que implementa
un analizador semántico completo para expresiones aritméticas con declaración
de variables `int`, cubriendo:

1.1 Árboles de expresiones
1.2 Acciones semánticas de un analizador sintáctico
1.3 Comprobación de tipos en expresiones
1.4 Pila semántica en un analizador sintáctico
1.5 Esquema de traducción (código de tres direcciones)
1.6 Generación de la tabla de símbolos y tabla de direcciones
1.7 Manejo de errores semánticos

## Requisitos

- JDK 17 o superior instalado (`java -version`).
- IntelliJ IDEA (Community o Ultimate).
- No necesitas internet para ejecutarlo: no hay dependencias externas.

## Cómo abrirlo en IntelliJ (Linux)

1. Descomprime el `.zip`.
2. Abre IntelliJ IDEA → **File > Open...** → selecciona la carpeta `semantic-analyzer`
   (la que contiene `pom.xml`). IntelliJ lo reconoce como proyecto Maven automáticamente.
3. Abre `src/main/java/com/itfcomalapa/semantico/Main.java`.
4. Haz clic en el triángulo ▶ verde junto a `public class Main` (o clic derecho →
   **Run 'Main.main()'**).

   Eso es todo — como es Swing puro, no hay module-path que configurar ni
   plugins adicionales que instalar.

## Uso del programa

- **Código fuente**: escribe declaraciones como:
  ```
  int a = 10;
  int b = 5;
  int c = 2;
  int d = 4;
  int resultado = a + b * c - d;
  ```
- **ANALIZAR**: ejecuta el análisis léxico, sintáctico y semántico completo.
- **LIMPIAR**: borra el código y los resultados.
- **EJEMPLO CORRECTO**: carga un ejemplo válido (igual al de la imagen de referencia).
- **EJEMPLO CON ERROR**: carga un ejemplo con variable no declarada, variable
  redeclarada y división entre cero, para probar el manejo de errores (1.7).

## Estructura del código

- `Lexer.java` — analizador léxico (tokens).
- `Parser.java` — analizador sintáctico-semántico: construye el árbol de
  expresión con una **pila semántica explícita** (operandos/operadores),
  comprueba tipos, genera acciones semánticas, código de tres direcciones
  y llena la tabla de símbolos. Aquí está casi toda la lógica del compilador.
- `Node.java` — nodo del árbol de expresión.
- `SymbolTable.java` / `SymbolEntry.java` — tabla de símbolos y direcciones.
- `SemanticException.java` — errores con número de línea.
- `Main.java` — interfaz gráfica en Swing (réplica del diseño de la imagen).

## Notas

- La gramática soportada es: `int ID = expr ;` con `expr` formada por
  `+ - * /`, paréntesis, identificadores y números enteros.
- Puedes extender fácilmente el analizador agregando más tipos de datos
  (`float`, `boolean`) o más instrucciones (if, while) siguiendo el mismo
  patrón usado en `Parser.java`.
