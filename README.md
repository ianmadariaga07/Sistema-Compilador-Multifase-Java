# Sistema Integral de Compiladores e Intérpretes: Arquitectura de Escáneres y Teoría de Autómatas

Este repositorio documenta el diseño, la base matemática y la implementación en código de un ecosistema de compilación desarrollado bajo estrictos estándares de ingeniería de software. Utilizando **Java** como lenguaje base, **JavaFX** para la visualización de datos y **MVC** como patrón arquitectónico, el sistema traduce modelos matemáticos abstractos en motores de análisis léxico y sintáctico completamente funcionales.

---

## Introducción al Análisis Léxico y Teoría de Autómatas

El análisis léxico constituye la fase inicial y más crítica en el proceso de compilación. Actúa como la primera barrera hermética entre el código fuente bruto (una cadena plana de caracteres) y el analizador sintáctico. Su objetivo fundamental es recorrer el flujo de entrada carácter por carácter, descartar elementos carentes de valor semántico (como espacios en blanco, tabulaciones y saltos de línea) y agrupar las secuencias válidas en unidades lógicas indivisibles denominadas **lexemas**, las cuales son clasificadas y encapsuladas en **tokens**.

Para lograr esta segmentación de manera eficiente y libre de ambigüedades, esta primera fase del proyecto se fundamenta en la teoría de los **Lenguajes Regulares** (Nivel 3 de la Jerarquía de Chomsky). La validación de los tokens se lleva a cabo mediante la construcción programática de **Autómatas Finitos Deterministas (AFD)**.

Desde el punto de vista matemático, cada programa de esta fase modela la tupla quíntuple $M = (Q, \Sigma, \delta, q_0, F)$, donde el código Java simula la función de transición de estados $\delta$ mediante estructuras de control iterativas (`while`) y condicionales (`switch-case`). El flujo de caracteres se somete a una matriz de estados donde se controla rigurosamente el estado inicial ($q_0$), los estados de transición, los estados terminales o de aceptación ($F$), y los **estados sumidero o de error**, garantizando un rechazo inmediato ante cualquier símbolo intruso que no pertenezca al alfabeto definido ($\Sigma$).

---

## ANALIZADOR LEXICO - FASE 1: Implementación Rigurosa de Autómatas Finitos Deterministas

A continuación, se detalla la base teórica y formal de los 7 motores léxicos desarrollados manualmente durante la primera etapa del proyecto, los cuales sientan las bases lógicas antes de la transición hacia herramientas de automatización industrial.

### PROGRAMA 1: AUTÓMATA FINITO DETERMINISTA: Patrón Binario Ceros
**Descripción formal del lenguaje:** Este autómata está diseñado para evaluar y reconocer lenguajes estrictos sobre un alfabeto binario. Su función es validar que una cadena cumpla con una expresión regular que exija iniciar y terminar con el carácter `0`, permitiendo un bloque intermedio del mismo carácter, denotado por la expresión `00*00`.

**Definición Matemática:**
* **Alfabeto ($\Sigma$):** $\{0, 1\}$
* **Conjunto de Estados ($Q$):** $\{q_0, q_1, q_2, q_3, q_E\}$
* **Estado Inicial ($q_0$):** $q_0$
* **Estados de Aceptación ($F$):** $\{q_3\}$

**Lógica de Implementación:**
El motor inicia en el estado base $q_0$. Al recibir un `0` transita al estado $q_1$, y mediante sucesivos ceros avanza hasta alcanzar el estado terminal $q_3$, donde la cerradura le permite ciclar infinitamente sobre el mismo símbolo. Dado que es un autómata determinista de alta restricción, la lectura de cualquier `1` en los estados críticos desvía la función de transición hacia $q_E$ (Estado de Error/Sumidero), del cual es imposible retornar, rechazando la cadena en tiempo real.

| Estado Actual | Lectura: 0 | Lectura: 1 |
| :--- | :---: | :---: |
| $\rightarrow q_0$ | $q_1$ | Error ($q_E$) |
| $q_1$ | $q_2$ | Error ($q_E$) |
| $q_2$ | $q_3$ | Error ($q_E$) |
| $* q_3$ | $q_3$ | Error ($q_E$) |

---

### PROGRAMA 2: AUTÓMATA FINITO DETERMINISTA: Detección de Subcadena "aa"
**Descripción formal del lenguaje:**
Motor de reconocimiento de patrones basado en un alfabeto alfabético cerrado. Su regla de negocio exige que, sin importar la longitud o la prefijación de la cadena, esta contenga obligatoriamente la subsecuencia adyacente `aa` en alguna parte de su estructura para ser validada.

**Definición Matemática:**
* **Alfabeto ($\Sigma$):** $\{a, b\}$
* **Conjunto de Estados ($Q$):** $\{q_0, q_1, q_2\}$
* **Estado Inicial ($q_0$):** $q_0$
* **Estados de Aceptación ($F$):** $\{q_2\}$

**Lógica de Implementación:**
El estado $q_0$ actúa como un bucle de espera consumiendo caracteres `b`. Al detectar la primera `a`, el autómata altera su estado hacia $q_1$. Si este estado detecta una interrupción (el símbolo `b`), la memoria temporal se reinicia devolviendo el grafo a $q_0$. Por el contrario, si $q_1$ empareja una segunda `a` consecutiva, el flujo desemboca en $q_2$, un estado terminal cerrado que absorberá cualquier carácter posterior (`a` o `b`), garantizando la aceptación absoluta de la cadena.

| Estado Actual | Lectura: a | Lectura: b |
| :--- | :---: | :---: |
| $\rightarrow q_0$ | $q_1$ | $q_0$ |
| $q_1$ | $q_2$ | $q_0$ |
| $* q_2$ | $q_2$ | $q_2$ |

---

### PROGRAMA 3: ANALIZADOR LÉXICO: Identificador de Variables
**Descripción formal del lenguaje:**
Implementación exacta de la regla lexicográfica universal empleada por los compiladores modernos para la reserva de nombres en espacios de memoria (variables, métodos y clases).

**Definición de la Expresión Regular:**
`[a-zA-Z_] [a-zA-Z0-9_]*`

**Lógica de Implementación:**
El autómata garantiza el cumplimiento de la directiva principal: un identificador jamás puede comenzar con un dígito.
1.  **Evaluación de Arranque:** El estado inicial exige incondicionalmente un símbolo alfabético (`A-Z`, `a-z`) o un guion bajo (`_`). Si el primer carácter es numérico (ej. `1variable`), se deniega el análisis.
2.  **Evaluación de Continuidad:** Una vez superado el primer carácter, el estado secundario de aceptación permite un ciclo infinito donde se combinan de manera indiferente letras y números, excluyendo tajantemente caracteres especiales (`@`, `#`, `$`, etc.).

---

### PROGRAMA 4: AUTÓMATA FINITO DETERMINISTA: Patrón Sufijo `(a|b)*abb`
**Descripción formal del lenguaje:**
Motor lexicográfico fundamentado en un alfabeto cerrado $\Sigma = \{a, b\}$, diseñado matemáticamente para auditar secuencias basándose exclusivamente en su terminación. La expresión regular requiere que la cadena posea un prefijo ilimitado de combinaciones, pero impone una regla de cierre estricta con el sufijo `abb`.

**Lógica de Implementación y Fallos Condicionales:**
El grado de complejidad de este autómata radica en la pérdida de la aceptación. Mientras el programa consume el prefijo `(a|b)*`, transita a través de sus estados buscando la secuencia final. Al encontrar `a` $\rightarrow$ `b` $\rightarrow$ `b`, el motor se posiona en el estado terminal de aceptación. Sin embargo, a diferencia del autómata de subcadenas, si la lectura no se ha detenido y se ingresa un carácter adicional después del sufijo esperado, el autómata es forzado a retroceder a sus estados iniciales, invalidando el bloque hasta que se vuelva a detectar la terminación exigida.

---

### PROGRAMA 5: ANALIZADOR LÉXICO: Expresión Regular `(0*1|1*)01`
**Descripción formal del lenguaje:**
Este analizador incrementa significativamente la complejidad estructural al incorporar ramificaciones lógicas. Implementa un autómata que satisface la expresión regular combinada `(0*1|1*)01`, demostrando el dominio algorítmico sobre la unión condicional (OR `|`) y las iteraciones infinitas (Cerradura de Kleene `*`).

**Definición Matemática:**
* **Alfabeto ($\Sigma$):** $\{0, 1\}$
* **Conjunto de Estados ($Q$):** $\{q_0, q_1, q_2, q_3, q_4, q_E\}$
* **Estados de Aceptación ($F$):** $\{q_4\}$

**Lógica de Implementación:**
El código simula la no-determinación inherente a la expresión regular bifurcando el análisis inicial. El motor debe estar preparado para absorber bloques continuos de ceros (`0*`) seguidos de un uno, o bloques continuos de unos (`1*`). Tras consumir este prefijo altamente variable, el autómata impone un candado estricto en los últimos dos niveles de su matriz: la lectura final debe ser forzosamente la secuencia `0` seguida de `1` para poder estacionarse en el estado de aceptación.

---

### PROGRAMA 6: ANALIZADOR LÉXICO: Notación Científica
**Descripción formal del lenguaje:**
Es la máquina de estados manual más sofisticada de la primera fase. Modelada para validar expresiones aritméticas representadas en notación científica o formato de coma flotante extendido (ej. `-3.1415e+10` o `0.5E-4`), respetando los estándares lexicográficos de lenguajes de alto nivel como Java o C++.

**Definición Matemática del Alfabeto:**
* **$\Sigma$:** $\{ 0, 1, ..., 9, ., e, E, +, - \}$

**Matriz de Estados (7 Niveles de Profundidad):**
El autómata traza un recorrido exhaustivo para evitar falsos positivos:
* **$q_0$ (Estado Inicial):** Intercepta signos aritméticos opcionales (`+`, `-`) o dígitos directos.
* **$q_1$ (Parte Entera):** Bucle de consumo de dígitos numéricos $D^+$.
* **$q_2$ y $q_3$ (Transición Decimal):** Exige estrictamente el símbolo `.` para transitar y comenzar a almacenar la mantisa fraccionaria.
* **$q_4$ (Salto Exponencial):** Identificador sensible `e` o `E` que activa la fase exponencial.
* **$q_5$ y $q_6$ (Estado de Aceptación):** Admite un nuevo signo opcional exclusivo para el exponente y sella la evaluación con una cadena numérica terminal.

---

### PROGRAMA 7: ANALIZADOR LÉXICO: Archivo de Configuración
**Descripción formal del lenguaje:**
Primer acercamiento hacia la construcción de escáneres funcionales para entornos de desarrollo del mundo real. Este motor está entrenado para procesar la gramática de archivos de propiedades y configuraciones (ej. archivos `.ini` o `.env`), interpretando estructuras del tipo `identificador = valor`.

**Lógica de Implementación y Lexemas:**
A diferencia de los autómatas binarios, este motor opera sobre múltiples alfabetos combinados y utiliza espacios en blanco como delimitadores. Su arquitectura se divide internamente en sub-autómatas:
1.  **Validación del L-Value:** Inicia consumiendo caracteres alfabéticos `[a-zA-Z]` para construir un identificador válido.
2.  **Transición Relacional:** Identifica la interrupción de la cadena mediante un espacio o el token relacional `=`, validando la sintaxis de asignación.
3.  **Validación del R-Value:** Tras el operador, evalúa que el lado derecho corresponda a una literal válida (numérica o alfanumérica). Cualquier ruptura en este orden secuencial dispara un error léxico instantáneo.

---

## ANALIZADOR SINTACTICO - SEGUNDA ETAPA (Procesamiento con JFlex) 
Para esta fase se emplea **JFlex**, un generador de analizadores léxicos para Java basado en autómatas finitos deterministas (DFA). A partir de un archivo de especificaciones lógicas (extensiones `.flex` o `.txt`), JFlex calcula las matrices de transiciones optimizadas y exporta una clase Java nativa que implementa el método secuencial `yylex()`.

### PROGRAMA 8: MIKE# (Analizador Léxico Puro)
Este módulo implementa un escáner genérico optimizado para la detección de estructuras comunes de lenguajes de programación imperativos, reconociendo palabras reservadas, identificadores, constantes numéricas enteros y delimitadores.

* **Definiciones Regulares Clave:**
    * `Letra = [a-zA-Z_]`
    * `Digito = [0-9]`
    * `Identificador = {Letra}({Letra}|{Digito})*`
    * `Numero = {Digito}+`
* **Encapsulamiento de Datos (`TokenModelo`):** Cada unidad léxica extraída se almacena en una instancia de `TokenModelo` con las siguientes propiedades:
    1.  `token`: Identificador de la categoría gramatical (ej. `INT`, `ASIGNACION`, `ID`).
    2.  `lexema`: Cadena textual exacta recuperada del buffer (ej. `x`, `=`, `100`).
    3.  `linea`: Índice de la línea física recuperado mediante `yyline + 1`.
    4.  `columna`: Ubicación exacta del carácter recuperado mediante `yycolumn + 1`.
* **Lógica del Controlador:** El controlador ejecuta un bucle iterativo controlado que invoca a `lexico.yylex()`. Los tokens válidos son inyectados en una lista observable (`ObservableList<TokenModelo>`) conectada de manera directa al `TableView` correspondiente. Ante la aparición de un componente inválido, el lexer retorna un estado `ERROR`, abortando la ejecución y detonando una alerta que detalla la coordenada de la anomalía léxica.

---

## Fase Sintáctica Descendente Recursiva (Parsers Top-Down Manuales)

El análisis sintáctico toma el flujo de tokens provisto por el escáner y determina si cumple con las reglas estructurales definidas por una Gramática Libre de Contexto (CFG). Los analizadores sintácticos descendentes recursivos (**DRR**) construyen el árbol de derivación desde el símbolo inicial (raíz) hacia los terminales (hojas) mediante llamadas recursivas a funciones lógicas, donde cada No Terminal de la gramática representa un método específico dentro del código.

### PROGRAMA 9: BOOLEAN (Sintáctico Booleano Básico)
Este componente valida la estructura lineal de expresiones booleanas elementales estructuradas con operadores de conjunción, disyunción y negación.
* **Gramática Formal Aplicada:**
    * E -> true E' | false E' | not E
    * E' -> and E | or E | λ
* **Implementación en Código:** Utiliza un puntero de anticipación léxica controlado por el método `avanzar()`. El método principal `evaluar()` inspecciona el token bajo el cursor; ante literales de verdad (`TRUE`/`FALSE`) transfiere el control a `E_Prima()`. Si intercepta una negación (`NOT`), efectúa una llamada recursiva sobre `evaluar()`. El método `E_Prima()` evalúa la continuidad secuencial de operadores (`AND`/`OR`). Si el flujo alcanza de forma limpia el delimitador `;` y la pila de ejecución recursiva se vacía sin anomalías, el veredicto es positivo.

### PROGRAMA 10: OPERACIONES (Sintáctico Aritmético con Precedencia)
Este módulo implementa un parser clásico para operaciones aritméticas infijas. Su objetivo principal es resolver la precedencia de operadores (*multiplicación/división* poseen prioridad frente a la *suma/resta*) y estructurar correctamente las jerarquías impuestas por el uso de paréntesis.
* **Gramática Factorizada por Precedencia:**
    * E -> T E'
    * E' -> + T E' | - T E' | λ
    * T -> F T'
    * T' -> * F T' | / F T' | λ
    * F -> ( E ) | numero | id
* **Lógica Operativa:** Mediante el mapeo modular de los métodos `E()`, `E_prima()`, `T()`, `T_prima()` y `F()`, la prioridad de ejecución matemática queda resuelta por la profundidad intrínseca de las llamadas en el stack: el sistema está obligado a resolver los factores básicos (`F`) y los términos de multiplicación (`T`) antes de ascender a los componentes de suma (`E`). El método `emparejar(Token esperado)` actúa como una compuerta estricta que lanza excepciones sintácticas ante desajustes en el flujo.

### PROGRAMA 11: BOOLEAN APRIMA (Eliminación de Recursividad por la Izquierda)
Una gramática que posee reglas recursivas directas por la izquierda de la forma (A -> A α) provoca un desbordamiento de pila (*Stack Overflow*) en un analizador descendente manual, ya que el método se invocaría a sí mismo de manera infinita sin consumir tokens. Este módulo demuestra la aplicación práctica de los algoritmos de factorización y eliminación de recursividad por la izquierda.
* **Gramática Original Ambigua:**
    * E -> E and T | E or T | T
* **Gramática Transformada (Factorizada):**
    * E -> T E'
    * E' -> and T E' | or T E' | λ
    * T -> not T | true | false | ( E )
* **Lógica Aplicada:** Las producciones vacías (λ) se gestionan en el entorno de desarrollo permitiendo que el método `E_prima()` finalice de manera limpia en caso de que el token analizado no pertenezca a los operadores lógicos esperados, permitiendo el retorno exitoso de la función.

## Fase Sintáctica Descendente Predictiva No Recursiva (ASDP - LL(1))

### PROGRAMA 12: ASDP
El Analizador Sintáctico Descendente Predictivo (**ASDP**) erradica el uso de llamadas recursivas en la pila de ejecución de Java, sustituyéndolas por una estructura basada en una **Pila (Stack) de datos explícita** controlada por el programador y una **Tabla de Transiciones Predictiva M**. Opera bajo el estándar de análisis **LL(1)** (Lectura de izquierda a derecha, derivación por la izquierda, utilizando un único token de anticipación).

### Componentes de la Arquitectura Predictiva

1.  **La Pila (Stack):** Estructura que almacena los símbolos gramaticales. Se inicializa cargando el delimitador de fondo de archivo `$` y el símbolo no terminal raíz `S`.
2.  **La Cadena de Entrada:** Vector secuencializado de tokens que finaliza de forma obligatoria con el símbolo `$`.
3.  **La Tabla de Predicción M:** Matriz bidimensional donde las filas representan los Símbolos No Terminales y las columnas corresponden a los Símbolos Terminales (Tokens). Las intersecciones definen de manera unívoca la regla de producción aplicable.

```text
                  TABLA DE PREDICCIÓN SINTÁCTICA M
+--------------+-----------+-----------+-----------+-----------+-----------+
| Símbolo N.T. |     a     |     b     |     c     |     d     |     ;     |
+--------------+-----------+-----------+-----------+-----------+-----------+
|      S       |  S -> A B |  S -> A B |           |           |           |
|      A       |  A -> a   |  A -> λ   |           |  A -> λ   |           |
|      B       |           | B -> b C d|           |           |           |
|      C       |           |           |  C -> c   |  C -> λ   |           |
+--------------+-----------+-----------+-----------+-----------+-----------+
```

### Algoritmo de Control Operativo
El motor ejecuta un ciclo iterativo continuo que compara el elemento posicionado en el tope de la pila (`X`) con el token actual bajo el cursor de entrada (`a`):
* **Aceptación Definitiva:** Si `X == a` y el valor evaluado equivale a `$`, la cadena se declara estructuralmente **Aceptada**.
* **Acción de Match (Emparejamiento):** Si `X == a` y corresponden a elementos terminales, se extrae `X` de la pila (`pop`) y el cursor de la entrada avanza a la siguiente posición.
* **Acción de Expansión:** Si `X` es un No Terminal, se consulta el índice `M[X, a]`:
    * Si la celda contiene una regla de producción (ej. `S -> A B`), se extrae `X` de la pila y se insertan los componentes de reemplazo en sentido **inverso** (primero `B`, posteriormente `A`), asegurando que el elemento izquierdo quede disponible en la cima.
    * Si la celda define un camino vacío o **Lambda** (ej. `A -> λ`), simplemente se extrae `X` de la pila sin efectuar inserciones adicionales.
    * Si la celda se encuentra vacía, se interrumpe el flujo y se cataloga como un error sintáctico.

### Captura Dinámica de Estados (`PasoASDP`)
Con el propósito de brindar una interfaz gráfica transparente y educativa, cada iteración del bucle instancia un objeto `PasoASDP` encargado de capturar una instantánea del estado del autómata:
* `pila`: Representación textual exacta de los elementos contenidos en el stack (ej. `[$, ;, d, C, b]`).
* `entrada`: Segmento remanente de tokens pendientes de procesamiento en el buffer (ej. `[b, c, d, ;]`).
* `accion`: Registro descriptivo del movimiento efectuado (ej. `Match 'b'`, `Aplicar Regla B -> b C d` o `Derivación C -> λ`).

Estos objetos pueblan un `TableView` exclusivo en la interfaz de JavaFX, desplegando el historial completo paso a paso.

---

## Fase Sintáctica Ascendente Profesional (CUP - Parsers Bottom-Up)

### PROGRAMA 13: BOOLEAN CUP (Evaluador LALR)
Este módulo representa la transición hacia el desarrollo de compiladores de nivel industrial. A diferencia de las metodologías Top-Down vistas en los cinco módulos previos, **CUP (Constructor of Useful Parsers)** implementa un algoritmo de análisis sintáctico ascendente **LALR**, ejecutando operaciones secuenciales de **Desplazamiento (Shift)** y **Reducción (Reduce)** para construir el árbol jerárquico desde las hojas (terminales) hacia la raíz.

### Integración JFlex-CUP y paso de Símbolos
Bajo este esquema, el analizador léxico interactúa directamente con el parser a través de un contrato formal dictado por la interfaz `java_cup.runtime.Scanner`.
Al compilar el archivo `.flex` incorporando la directiva dedicada `%cup`, el método `next_token()` se reestructura para retornar instancias de **`java_cup.runtime.Symbol`**. Este contenedor transporta tres datos fundamentales:
* `id`: Atributo entero representativo de la categoría del token (generado automáticamente por CUP en la clase intermedia `symBooleanoCup.java`).
* `left` / `right`: Punteros de coordenadas que mapean la ubicación exacta de línea y columna del lexema para la trazabilidad de errores.
* `value`: Atributo genérico de tipo `Object` que encapsula el valor semántico real transformado en tipos nativos de Java (ej. `Boolean` o `String`).

### Directivas Semánticas y Precedencia en `booleano.cup`
CUP trasciende la simple validación formal al permitir la ejecución de **acciones semánticas activas** integradas directamente en las directivas de reducción. El archivo de especificación `booleano.cup` anula la ambigüedad gramatical declarando de manera explícita los niveles asociativos y de precedencia de los operadores. Mediante el uso de la variable reservada `RESULT`, el parser propaga el resultado de las evaluaciones lógicas a través de los nodos del árbol.

```cup
package fes.aragon.sintactico;
import java_cup.runtime.*;

parser code {:
    // Variable global destinada a almacenar el veredicto para la interfaz gráfica
    public String resultadoFinal = "";

    @Override
    public void syntax_error(Symbol s) {
        resultadoFinal = "Error sintáctico: No se esperaba el token '" + s.value + "' en la línea " + (s.left + 1) + " columna " + (s.right + 1);
    }
:};

/* Sección de Terminales (Vocabulario Léxico) */
terminal PUNTOYCOMA, OR, AND, NOT, LPAREN, RPAREN;
terminal Boolean TRUE, FALSE;

/* Sección de No Terminales (Reglas de Producción) */
non terminal expr_lista, expr_parte;
non terminal Boolean expr;

/* Declaración de Precedencia (De menor a mayor prioridad) */
precedence left OR;
precedence left AND;
precedence right NOT;

/* Cuerpo de la Gramática y Acciones Semánticas Activas */
expr_lista ::= expr_lista expr_parte
             | expr_parte ;

expr_parte ::= expr:e PUNTOYCOMA 
             {: 
                parser.resultadoFinal = "Análisis LALR Exitoso. Resultado evaluado: " + e.toString().toUpperCase(); 
             :};

expr ::= expr:e1 OR expr:e2   {: RESULT = e1 || e2; :}
       | expr:e1 AND expr:e2  {: RESULT = e1 && e2; :}
       | NOT expr:e           {: RESULT = !e; :}
       | TRUE:t               {: RESULT = t; :}
       | FALSE:f              {: RESULT = f; :}
       | LPAREN expr:e RPAREN {: RESULT = e; :};
```

Cuando el autómata de CUP ejecuta una **reducción**, extrae los valores asignados a los componentes de la subregla (`e1`, `e2`), computa la operación lógica nativa de Java (`||`, `&&`, `!`) y almacena el producto en `RESULT`. Al completarse la reducción de la regla raíz, el valor lógico final se escribe en `resultadoFinal`, quedando inmediatamente disponible para que el controlador lo despliegue en la interfaz gráfica.

---

## Diseño de Interfaz Gráfica (JavaFX UX/UI)

La vista de la aplicación está definida en un entorno FXML lineal que prioriza la legibilidad del flujo de información, complementado por estilos centralizados en un archivo `style.css` para aplicar propiedades estéticas sobrias y corporativas.

### Resolución de Solapamiento de Contenedores de Datos
Para maximizar el uso eficiente del área de visualización y proporcionar un entorno unificado, las dos tablas (`TableView`) dedicadas a los resultados léxicos y al trazado de la pila del ASDP comparten exactamente las mismas coordenadas espaciales dentro de la ventana. Debido a que las cabeceras de tabla nativas de JavaFX poseen un canal de opacidad con transparencia parcial por defecto, la superposición directa generaba artefactos visuales donde los textos del componente inferior se traslucían de manera incorrecta.

* **Estrategia de Solución:**
    1.  **Sincronización Matemática de Layout:** En la especificación FXML se asignaron propiedades idénticas de posicionamiento espacial (`Layout X`, `Layout Y`) y de dimensiones estáticas (`Pref Width`, `Pref Height`) a ambos nodos de tabla.
    2.  **Ocultación Estática en Diseño:** Se desmarcó la propiedad nativa `Visible` en el panel de SceneBuilder para la grilla secundaria, manteniendo el entorno de edición visual completamente limpio y libre de textos superpuestos.
    3.  **Conmutación Dinámica por Software:** En `InicioController.java` se implementaron funciones de alternancia de estado de visibilidad:
        ```java
        private void prepararInterfazParaTabla() {
            tablaTokens.setVisible(true);
            txtAreaResultado.setVisible(false);
            tablaASDP.setVisible(false);
        }
        private void prepararInterfazParaASDP() {
            tablaTokens.setVisible(false);
            txtAreaResultado.setVisible(false);
            tablaASDP.setVisible(true);
        }
        ```
        Al dispararse el switch de validación, el sistema ejecuta el método correspondiente, modificando el estado de visibilidad de los nodos gráficos de forma limpia y transparente para el usuario.

### Implementación del Sistema de Ayuda Mediante el Patrón Diccionario
Con el fin de proveer asistencia inmediata y documentar la base teórica y el modo de uso de cada uno de los seis analizadores implementados, se estructuró un framework de ayuda interactiva en el método de inicialización del controlador.

Para evitar un bloque condicional masivo y desordenado dentro de los eventos de la vista, se aplicó el patrón de diseño **Diccionario** utilizando colecciones basadas en mapas estructurados (`java.util.Map` y `java.util.HashMap`):

1.  **Instanciación del Diccionario:**
    ```java
    private final java.util.Map<String, String[]> infoAnalizadores = new java.util.HashMap<>();
    ```

Esta solución separa por completo los datos textuales informativos de la lógica de control del flujo de eventos. Si en fases futuras se requiere expandir el catálogo de analizadores o refinar la teoría, las modificaciones se restringen al diccionario sin alterar la función encargada de detonar los cuadros de diálogo.

---

## Infraestructura de Construcción y Portabilidad (Maven & JPMS)

El despliegue del proyecto descansa sobre dos pilares de configuración profunda diseñados para conciliar dependencias del año 2006 con las directivas de seguridad del SDK de Java moderno.

### Enlace de Dependencias Mediante Rutas Relativas en Maven
El motor de ejecución de CUP (`java-cup-11.jar`) no reside de forma pública en los repositorios centrales globales de Maven tradicionales. Para evitar el uso de rutas absolutas locales vinculadas estrictamente a una estación de trabajo (lo que provocaría fallas inmediatas de compilación al mover el proyecto a otro equipo), se configuró un enlace portable usando propiedades del sistema de Maven:

## Guía de Ejecución en Terminal y Batería de Pruebas Oficial

Para regenerar las clases lógicas de los analizadores desde las especificaciones formales de JFlex y CUP, se deben introducir los siguientes comandos posicionando la terminal en la raíz de la carpeta `AnalizadorSintactico`:

### Comandos de Compilación de Componentes

* **Comando de Generación Sintáctica (CUP):**
    ```bash
    java -jar jar/java-cup-11.jar -parser SintacticoBooleanoCup -symbols symBooleanoCup -destdir src/main/java/fes/aragon/sintactico src/main/resources/fes/aragon/codigosJFlexCup/booleano.cup
    ```
* **Comando de Generación Léxica (JFlex):**
    ```bash
    java -jar jar/jflex-full-1.9.1.jar -d src/main/java/fes/aragon/lexico src/main/resources/fes/aragon/codigosJFlexCup/lexicoBooleanoCup.txt
    ```

---

### Casos de Prueba del Entorno Analítico

Para verificar la estabilidad y el correcto funcionamiento de cada analizador, se recomienda introducir los siguientes datos muestra dentro de la aplicación:

#### Programa 1: MIKE#
* **Entrada de Éxito:** `int total = 45 ;`
    * *Resultado Esperado:* Desglose ordenado de los 4 elementos en la grilla interactiva detallando identificadores y asignaciones.
* **Entrada de Falla:** `int error = 12$ ;`
    * *Resultado Esperado:* Interrupción inmediata y despliegue de una alerta que reporta la presencia del componente no admitido `$`.

#### Programa 2: BOOLEAN
* **Entrada de Éxito:** `true or not false ;`
    * *Resultado Esperado:* Salida de confirmación limpia indicando una sintaxis correcta en el panel de texto principal.
* **Entrada de Falla:** `true and or ;`
    * *Resultado Esperado:* Lanzamiento de error debido al posicionamiento contiguo de dos operadores binarios sin un operando intermedio.

#### Programa 3: OPERACIONES
* **Entrada de Éxito:** `(4 + 6) * 3 / 2 ;`
    * *Resultado Esperado:* Validación exitosa de las prioridades aritméticas de multiplicación y resolución del paréntesis.

#### Programa 5: ASDP (Análisis LL(1))
* **Caso de Éxito Estándar:** `a b c d ;`
    * *Resultado Esperado:* Vaciado secuencial de la pila visible paso por paso en el `TableView` hasta alcanzar la aceptación.
* **Caso de Éxito por Derivación Vacía:** `a b d ;`
    * *Resultado Esperado:* El motor detecta la ausencia de la letra `c` y aplica de manera exitosa la regla (C -> λ), extrayendo el elemento de la pila sin consumir caracteres de entrada.
* **Caso de Falla Sintáctica:** `c d ;`
    * *Resultado Esperado:* Reporte inmediato de error. El autómata consulta la coordenada vacía `M[B, c]` y aborta el análisis al no detectar el inicio requerido de la letra `b`.

#### Programa 6: BOOLEAN CUP (Evaluador LALR)
* **Caso Semántico A:** `not false and false ;`
    * *Resultado Semántico Evaluado:* **FALSE**. CUP procesa de manera correcta la prioridad más alta del operador `NOT`, convirtiendo el fragmento inicial en `true`, resolviendo posteriormente la conjunción lógica `true AND false`.
* **Caso Semántico B:** `not ( false and false ) ;`
    * *Resultado Semántico Evaluado:* **TRUE**. El motor evalúa el interior de la agrupación dando un valor intermedio de `false`, el cual es negado y transformado en una afirmación verdadera absoluta por el operador externo.
* **Caso de Error Controlado:** `true or ;`
    * *Resultado:* Despliegue de un mensaje controlado en la interfaz: *“Error sintáctico: No se esperaba el token ';' en la línea 1 columna 9”*, disparado desde la función sobreescrita de gestión de errores del parser autogenerado.