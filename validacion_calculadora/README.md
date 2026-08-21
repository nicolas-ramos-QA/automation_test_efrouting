# validacion_calculadora

Automatización E2E (Serenity BDD + Screenplay + Cucumber) para validar la calculadora del **Loadboard**.

## Flujo

1. Login.
2. Navegar a **Loadboard**.
3. Buscar cargas con **origen y destino variables** (`data.json → city_pairs`).
4. Clic en **Search loads** y, al llegar a resultados, **esperar hasta 15 s** (cerrando overlays
   tipo Recommended / Next Lane) a que carguen las cargas.
5. Validar el **punto verde en Rate** solo dentro de `data-cy=load-item-ratePerMile`
   (no el punto de Connectivity). Si hace falta, scrollea la lista virtualizada.
   - Si no hay ninguno → **cambia origen/destino** (empieza por Chicago → Miami) y reintenta.
6. Abrir una carga con **punto verde** → modal **Load details**.
7. Validar texto verde de negociación; luego **Calculate operation** → **Current profit** → fórmula RPM.

## Ejecución

```powershell
cd C:\Users\USUARIO\Documents\automation_test_efrouting\automation_test_efrouting\validacion_calculadora
mvn test "-Dtest=RunnerValidacionCalculadora"
```

Reporte Serenity: `target/site/serenity/index.html`.

Tras una validación OK se genera además:

- HTML: `target/reportes/reporte-calculadora-ultimo.html` (se abre solo)
- Captura PNG del modal al final del reporte: `target/reportes/captura-calculadora-*.png`

El HTML incluye origen/destino, Current profit (ciudades + %), Income / Distance / RPM,
resultado exacto de la fórmula y la captura para comparar visualmente.

**Timeout:** el paso de abrir/validar cargas corta a los **3 minutos** si no logra
una validación completa (falla el escenario en lugar de scrollear hasta cerrar Chrome).

## Datos

Credenciales y pares de ciudades en `src/test/resources/data.json`.
