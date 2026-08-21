# flujo_creacion_ruta

Automatización E2E con Serenity BDD, Screenplay y Cucumber para validar el flujo de creación de una ruta y verificar las millas mostradas en línea.

## Casos cubiertos

El Scenario Outline ejecuta **cuatro** variantes según el tipo de ruta sugerida en Easy routes:

| tipoRuta | Comportamiento |
|----------|----------------|
| Tri-hauls | Si aparece → continúa (editar lane + millas). Si no → **omitido** (no fallido). |
| Bi-hauls | Igual. |
| Best Choice | Igual (incluye variantes como "★ Best Choice"). |
| Loops | Igual. |

Cuando Easy routes ya cargó (p. ej. solo Best Choice y Loops, como en la UI), los tipos ausentes (Bi-hauls / Tri-hauls) se **anulan al momento** sin esperar el timeout completo.

## Requisitos

- JDK 17
- Maven 3.8 o superior
- Google Chrome
- Acceso al ambiente de efRouting

## Ejecución

Todos los comandos deben ejecutarse desde esta carpeta:

```powershell
cd flujo_creacion_ruta

# Runner específico del flujo (4 escenarios: Tri-hauls, Bi-hauls, Best Choice, Loops)
mvn test "-Dtest=TestRunnerFlujoCreacionRuta"

# Todas las features de este proyecto
mvn test "-Dtest=TestRunner"
```

El reporte Serenity queda en `target/site/serenity/index.html`.

## Datos de prueba

Los datos usados por la automatización están en `src/test/resources/data.json`. Las rutas internas, como `src/test/resources/files/test.pdf`, son relativas a esta carpeta; por eso Maven debe ejecutarse desde `flujo_creacion_ruta`.

## Estructura

- `src/main/java/.../interactions`: acciones Screenplay.
- `src/main/java/.../question`: lecturas y verificaciones de la interfaz.
- `src/test/java/.../ranners`: runners de Cucumber.
- `src/test/java/.../stepdefinitions`: definiciones de pasos.
- `src/test/resources/features`: escenarios Gherkin.
