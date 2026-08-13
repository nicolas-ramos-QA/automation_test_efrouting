# calculos_de_rutas

Automatización E2E (Serenity BDD + Screenplay + Cucumber) que replica el flujo de creación de ruta de `automation_test_efrouting` **hasta "Try this route"** y, en lugar de validar millas, valida la integridad financiera Backend ↔ Frontend.

El mismo escenario se ejecuta contra **QA** y contra **Producción**, cada uno con su URL, sus credenciales y su propio reporte.

## Qué valida

1. **Interceptación de red** del endpoint `user-route/{id}?features[]=markAsViewed` (hooks de `fetch`/`XHR` inyectados en el navegador). Entre todas las llamadas a `user-route` se elige la que trae el bloque financiero.
2. **Cierre del panel de mapa.** Con el mapa abierto la tabla se comprime y efRouting solo renderiza Origin, Status, Mileage, Driving time, Income, RPM y Total cost. El botón hamburguesa que está junto a *Edit* lo cierra y recién ahí aparecen Fuel cost, Toll cost, Custom cost, Op cost y Profit.
3. **Por cada lane**: Income (rango), Fuel cost, Toll cost, Custom cost, Op cost, Total cost y Profit (rango) contra el JSON.
4. **Coherencia interna del Backend**: `totalCost = fuelCost + tollCost + customCost` y `profit.min/max = income.min/max − totalCost`.
5. **Op cost visible y oculto**: se verifica que el Total cost en pantalla incluya el Op cost y, al ocultarlo con el icono de ojo, que el Total cost baje exactamente ese importe y el Profit suba lo mismo.
6. **Valores generales de la ruta** (la fila rotulada *Total*): se comparan contra el bloque `finance` de la ruta y, además, contra la sumatoria de las lanes, para distinguir un error de agregación del Backend de uno de pintado.

Las comparaciones toleran el redondeo visual de la UI y el hecho de que efRouting muestra los costos por lane en negativo mientras el Backend los expone en positivo.

## Cómo calcula efRouting

Reglas deducidas del response y verificadas contra la pantalla:

| Concepto | Regla |
|----------|-------|
| Op cost de una lane | `finance.operativeCost` (tarifa por milla) × millaje de la lane. No viene por lane en el JSON. |
| Total cost de una lane | `finance.totalCost` de la lane (fuel + toll + custom) **+ Op cost**. |
| Profit de una lane | `finance.profit` de la lane **− Op cost**, mostrado como rango. |
| Op cost en la fila Total | La **tarifa por milla**, no la suma de los Op cost de las lanes. |
| Total cost en la fila Total | `finance.totalCost` de la ruta, que ya incluye tarifa × millaje total. |
| Profit en la fila Total | `finance.profit` de la ruta, que **no** descuenta el Op cost. |

Ese último punto es una asimetría del producto: el Total cost general incluye el Op cost pero el Profit general no lo resta, a diferencia del Profit por lane. La automatización no lo trata como fallo, lo registra como **ADVERTENCIA** en el reporte.

Una celda de costo que muestra el botón **"Add +"** (habitualmente Custom cost) significa que la columna existe y vale 0 porque nadie cargó un importe manual; se compara como cero y el reporte deja la aclaración.

## Reporte de cálculos

Al terminar el escenario se genera un reporte detallado con **cada** comparación realizada, separado por ambiente:

- `target/reportes/reporte-calculos-qa-ultimo.html` / `reporte-calculos-produccion-ultimo.html` — última corrida
- `target/reportes/reporte-calculos-<ambiente>-<timestamp>.html` — histórico

Cada fila muestra: **Estado** (PASÓ / FALLÓ / ADVERTENCIA / OMITIDO), tramo, campo, valor del **Backend**, valor del **Frontend** (con el texto tal como aparece en pantalla), la **diferencia** y el **cálculo aplicado**, por ejemplo `fuel 104.75 + toll 0.00 + custom 0.00 + op 255.49 = 360.24`.

Las validaciones son *soft*: no se detienen en el primer error, así el reporte muestra el panorama completo y el escenario falla al final listando todas las discrepancias. El mismo resumen queda adjunto en el reporte Serenity y en la consola. Si un paso previo falla, el hook `@After` igualmente escribe el reporte con lo validado hasta ese punto.

## Diagnóstico

Cada corrida deja en `target/diagnostico/`:

- `user-route.json` — el response financiero elegido
- `user-route-todas.txt` — todas las llamadas `user-route` capturadas
- `dom-detalle-ruta.html` — el DOM al interceptar la respuesta
- `dom-validacion-lanes.html` — el DOM en el momento exacto de leer la tabla

Es lo que hay que revisar primero cuando cambian los selectores o la estructura del response.

## Estructura

Misma ideología que el proyecto hermano:

- `interactions/` — acciones Screenplay
- `question/LaneTableReader` — lectura de la tabla de lanes y de la fila de valores generales
- `steps/` + `stepdefinitions/` — capa Cucumber
- `abilities/CaptureUserRouteApi` — captura JSON de `user-route`
- `models/` + `utils/RouteFinancialParser` — parseo del response
- `utils/FinancialAssertions` + `utils/FinancialReport` — comparaciones y reporte

## Cómo ejecutar

```bash
cd calculos_de_rutas

# Ambos ambientes
mvn clean test -Dtest=TestRunnerCalculosDeRutas

# Solo QA
mvn test -Dtest=RunnerCalculosQa

# Solo Producción
mvn test -Dtest=RunnerCalculosProduccion
```

En PowerShell hay que entrecomillar los parámetros: `mvn test "-Dtest=RunnerCalculosQa"`.

Reportes Serenity: `target/site/serenity`

## Datos de prueba

- Ambientes (URL, usuario, contraseña): `src/test/resources/data.json` → `data.environments`
- Origen, tráiler y demás datos del formulario: mismo archivo, bloque `data.input`

## Selectores

La tabla real es `table[data-cy='lanes-table']`: `thead th[data-column-id]`, filas `tbody tr[data-cy='principal-table-row-{laneId}']` con celdas `td[data-cy='lane-table-cell-{columnId}']` y la fila de valores generales en `tfoot`.

Las columnas se ubican por la posición que ocupan en el `thead`, reconocidas por su `data-column-id` o, si el frontend lo cambia, por el rótulo visible. Por eso alcanza con actualizar el enum `models/FinancialColumn` cuando cambia la convención de nombres.

## Diferencias vs automation_test_efrouting

| Incluido | Omitido |
|----------|---------|
| Login → Routes → New route → Trailer → form → Continue → Tri-hauls → **Try this route** | Edit lane |
| Validación financiera API ↔ UI en QA y Producción | Validación de millas (`LaneMileage` / `GeoDistance`) |
