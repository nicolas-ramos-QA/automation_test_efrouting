# automation_test_efrouting

Automatización E2E de efRouting con **Serenity BDD + Screenplay + Cucumber** sobre Selenium/Chrome.

Un solo proyecto Maven (`pom.xml` en la raíz) y **un solo `src`**. Los cuatro flujos conviven como paquetes y features distintas; los casos de prueba no se mezclan: cada uno se lanza con su runner.

| Paquete / features | Qué valida |
|--------------------|------------|
| `com.automation_test_efrouting` · `features/flujo_creacion_ruta` | Creación de ruta y millaje (Login → Routes → New route → Trailer → formulario → Continue → ruta sugerida → Edit lane). |
| `com.calculos_de_rutas` · `features/calculos_de_rutas` | Integridad financiera Backend ↔ Frontend (Income, costos, Profit por lane y Total, Op visible/oculto). QA, Producción, o una **ruta ya existente**. |
| `com.validacion_calculadora` · `features/validacion_calculadora` | Loadboard: 5 fórmulas del modal *Calculate profit*. |
| `com.filtros_de_rutas` · `features/filtros_de_rutas` | Listado Routes en QA (`/route-planner`): los 12 filtros del panel *Filter* y que el pie (Total routes, income, miles, DH, RPM) cambie. |

```
src/
  main/java/com/
    automation_test_efrouting/   # flujo creación
    calculos_de_rutas/           # cálculos financieros
    validacion_calculadora/      # calculadora Loadboard
    filtros_de_rutas/            # filtros del listado Routes
  test/java/com/                 # steps, runners y unit tests de cada flujo
  test/resources/
    data.json                    # datos de los cuatro flujos
    features/flujo_creacion_ruta/
    features/calculos_de_rutas/
    features/validacion_calculadora/
    features/filtros_de_rutas/
```

## Requisitos previos

1. **JDK 17** (`java -version` debe mostrar 17.x).
2. **Apache Maven 3.8+** (`mvn -version`).
3. **Google Chrome** instalado. Serenity descarga el `chromedriver` (`webdriver.autodownload = true`).
4. Acceso a QA (`https://efdata-qa.efrouting.com`) y/o Producción (`https://efdata.efrouting.com`).
5. Credenciales en `src/test/resources/data.json`.

## Compilar

```bash
git clone https://github.com/nicolas-ramos-QA/automation_test_efrouting.git
cd automation_test_efrouting
mvn -q dependency:resolve compile test-compile
```

Todos los comandos se ejecutan **desde la raíz** del repo.

## Cómo ejecutar

En PowerShell, entrecomillar los `-D`.

### flujo_creacion_ruta (millaje)

```powershell
mvn test "-Dtest=TestRunnerFlujoCreacionRuta"
# o
mvn test "-Dtest=TestRunner"
```

### calculos_de_rutas (validación financiera)

```powershell
mvn test "-Dtest=RunnerCalculosQa"
mvn test "-Dtest=RunnerCalculosProduccion"
mvn test "-Dtest=RunnerCalculosRutaExistente" "-Druta=https://efdata-qa.efrouting.com/route-planner/detail/3417"
```

Reportes propios: `target/reportes/reporte-calculos-<ambiente>-*-ultimo.html`.
Diagnóstico: `target/diagnostico/`.

### validacion_calculadora (Loadboard)

```powershell
mvn test "-Dtest=RunnerValidacionCalculadora"
```

Reporte propio: `target/reportes/reporte-calculadora-ultimo.html`.
Timeout del paso: `-Dcalculadora.timeout.min` (por defecto 10 minutos).

### filtros_de_rutas (listado Routes)

```powershell
mvn test "-Dtest=RunnerFiltrosDeRutas"
```

Solo QA: `https://efdata-qa.efrouting.com/route-planner`. Aplica los 12 filtros del panel.

Reporte propio: `target/reportes/reporte-filtros-ultimo.html`.

Reporte Serenity de cualquier corrida: `target/site/serenity/index.html`.

`mvn test` sin `-Dtest` solo toma clases `TestRunner*` (creación de ruta y el runner combinado de cálculos). Los runners `Runner*` hay que pedirlos explícitamente, igual que antes.

## Datos de prueba

Un único `src/test/resources/data.json` con ambientes, formulario de ruta, pares de ciudades del Loadboard, filtros de Routes y textos de UI. Si rotan credenciales, se actualizan ahí.

> Este archivo está versionado con credenciales reales. El repositorio debe permanecer **privado**.

## Estructura de ramas

- `qa`: rama de trabajo. Los merges a otras ramas los coordina Nicolás.

## Notas

- Si falla la descarga de dependencias, revise proxy corporativo hacia `repo.maven.apache.org` y repos de Serenity.
- Si el test financiero no encuentra una columna, revise `target/diagnostico/*.html` antes de tocar selectores.
- Los paquetes de cada flujo son independientes: no se reutilizan steps ni se reescribieron los escenarios.
