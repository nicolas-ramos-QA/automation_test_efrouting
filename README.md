# automation_test_efrouting

Automatización E2E de efRouting con **Serenity BDD + Screenplay + Cucumber** sobre Selenium/Chrome.

El repositorio contiene **tres proyectos Maven independientes** (cada uno con su propio `pom.xml`):

| Carpeta | Qué valida |
|---------|------------|
| [`flujo_creacion_ruta/`](flujo_creacion_ruta/) | Flujo de creación de ruta (Login → Routes → New route → Trailer → formulario → Continue → ruta sugerida → Edit lane) y la validación de millaje. |
| [`calculos_de_rutas/`](calculos_de_rutas/README.md) | Mismo flujo hasta "Try this route" (Tri-hauls, Bi-hauls, Best Choice o Loops; si el tipo no aparece se omite), pero valida la **integridad financiera Backend ↔ Frontend**: Income, Fuel/Toll/Custom/Op cost, Total cost y Profit por lane y en la fila Total, con Op cost visible y oculto. Corre contra QA y Producción. |
| [`validacion_calculadora/`](validacion_calculadora/README.md) | Login → **Loadboard** (no Routes). Base para validar la calculadora del loadboard. |

## Requisitos previos

Quien clone el repo necesita instalado localmente:

1. **JDK 17** (`java -version` debe mostrar 17.x).
2. **Apache Maven 3.8+** (`mvn -version`). El repo no trae `mvnw`, así que Maven debe estar en el `PATH`.
3. **Google Chrome** instalado (versión reciente). Serenity descarga el `chromedriver` automáticamente (`webdriver.autodownload = true`), pero necesita el navegador real para lanzarlo.
4. Conexión de red a los ambientes de efRouting que se van a probar:
   - QA: `https://efdata-qa.efrouting.com`
   - Producción: `https://efdata.efrouting.com`
5. Credenciales válidas de acceso a esos ambientes (ver [Datos de prueba](#datos-de-prueba)).

## Clonar y compilar

```bash
git clone https://github.com/nicolas-ramos-QA/automation_test_efrouting.git
cd automation_test_efrouting

# Proyecto de creación de ruta y millaje
cd flujo_creacion_ruta
mvn -q dependency:resolve compile test-compile
cd ..

# Proyecto de cálculos financieros
cd calculos_de_rutas
mvn -q dependency:resolve compile test-compile
cd ..

# Proyecto de calculadora en Loadboard
cd validacion_calculadora
mvn -q dependency:resolve compile test-compile
```

## Cómo ejecutar

### flujo_creacion_ruta (creación de ruta / millaje)

```bash
cd flujo_creacion_ruta

# Windows PowerShell: entrecomillar los parámetros -D
mvn test "-Dtest=TestRunner"
# o el runner específico del flujo de creación de ruta
mvn test "-Dtest=TestRunnerFlujoCreacionRuta"
```

Reportes Serenity: `flujo_creacion_ruta/target/site/serenity/index.html`.

### calculos_de_rutas (validación financiera)

```bash
cd calculos_de_rutas

# Solo QA (Tri-hauls, Bi-hauls, Best Choice y Loops; los ausentes se omiten)
mvn test "-Dtest=RunnerCalculosQa"

# Solo Producción
mvn test "-Dtest=RunnerCalculosProduccion"
```

Cada corrida genera:

- **Reporte HTML propio** (con capturas de la tabla, Op cost visible/oculto): `calculos_de_rutas/target/reportes/reporte-calculos-<ambiente>-<tipo-ruta>-ultimo.html`. Se abre automáticamente en el navegador al terminar el escenario.
- **Reporte Serenity estándar**: `calculos_de_rutas/target/site/serenity/index.html`.
- **Diagnóstico** (DOM y JSON capturados) en `calculos_de_rutas/target/diagnostico/`.

Más detalle de qué valida y cómo leer el reporte en el [README de calculos_de_rutas](calculos_de_rutas/README.md).

### validacion_calculadora (Loadboard)

```bash
cd validacion_calculadora
mvn test "-Dtest=RunnerValidacionCalculadora"
```

Reporte Serenity: `validacion_calculadora/target/site/serenity/index.html`.

## Datos de prueba

Las URLs, usuarios y contraseñas están en cada proyecto (`flujo_creacion_ruta`, `calculos_de_rutas`, `validacion_calculadora`) dentro de `src/test/resources/data.json`. Si las credenciales rotan o dejan de funcionar, hay que actualizarlas ahí — no hay variables de entorno de por medio.

> Estos `data.json` están versionados con credenciales reales de QA/Producción. Este repositorio debe permanecer **privado** y solo con acceso al equipo; no lo hagas público ni lo copies a otro lugar sin limpiar antes esas contraseñas.

## Estructura de ramas

- `qa`: rama de trabajo donde se sube la automatización mientras se valida. Los merges a otras ramas (`main`, `develop`, etc.) los coordina Nicolás.

## Notas para quien recién clona

- Si `mvn` falla descargando dependencias, revisa que no haya un proxy corporativo bloqueando `repo.maven.apache.org` ni los repos de `serenity-bdd`.
- Si Chrome abre pero el test financiero no encuentra un botón/columna, primero revisa `calculos_de_rutas/target/diagnostico/*.html` (el DOM real capturado en ese punto) antes de tocar los selectores en `userinterface/SelectorConstant.java`.
- En Windows PowerShell, los parámetros `-Dtest=...` con comas o espacios deben ir entre comillas dobles, ej. `mvn "-Dtest=A,B,C" test`.
