Feature: Validacion de calculos financieros de una ruta

  El mismo flujo se ejecuta contra QA y contra Produccion. Cada ambiente usa su propia URL y
  credenciales (data.json > data.environments) y genera su propio reporte de calculos.

  Se cubren cuatro casos al crear la ruta, forzando cada tipo de ruta sugerida:
  Tri-hauls, Bi-hauls, Best Choice y Loops. Si Easy routes no trae el tipo pedido
  (p.ej. solo hay Best Choice y Loops), ese escenario se anula (omitido) y no se
  marca como fallido. Si el tipo sí está disponible, el flujo continúa igual
  haciendo los cálculos (por lane y Total, con Op cost visible y oculto).

  Fórmulas validadas (redondeo Backend milésimas → entero UI):
  - Total cost = Fuel + Toll + Custom + Op cost (solo si el ojo está visible)
  - Profit     = Income − Total cost
  - Custom "Add +" = $0
  El reporte HTML se abre al terminar cada escenario (uno por ambiente y tipo de ruta),
  con la comparación primero por lane y después la fila Total (Income → Profit).

  @Regression @CalculosDeRutas
  Scenario Outline: Crear ruta tipo <tipoRuta> y validar integridad financiera Backend vs Frontend en <ambiente>
    Given Usuario se encuentra en la pantalla de inicio de sesión de "<ambiente>"
    When Usuario ingresa las credenciales de "<ambiente>"
    And Usuario selecciona el botón "Log in"
    And Usuario se dirige a la sección "Routes"
    And Usuario selecciona el botón "New route"
    And Usuario selecciona la opción "Trailer type only"
    And Usuario completa los datos de la nueva ruta
    And Usuario finaliza la creación seleccionando "Continue"
    And Usuario selecciona una ruta sugerida de tipo "<tipoRuta>"
    And Usuario cierra el mapa para ver todas las columnas de la tabla
    And Usuario intercepta la respuesta financiera del endpoint user-route
    And Usuario valida los valores financieros de cada lane contra el Backend
    And Usuario valida los valores generales de la ruta contra el Backend
    And Usuario valida el comportamiento del Op cost oculto
    Then Usuario genera el reporte detallado de los cálculos financieros

    @QA
    Examples:
      | ambiente | tipoRuta    |
      | QA       | Tri-hauls   |
      | QA       | Bi-hauls    |
      | QA       | Best Choice |
      | QA       | Loops       |

    @Produccion
    Examples:
      | ambiente   | tipoRuta    |
      | PRODUCCION | Tri-hauls   |
      | PRODUCCION | Bi-hauls    |
      | PRODUCCION | Best Choice |
      | PRODUCCION | Loops       |
