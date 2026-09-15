Feature: Validacion financiera de una ruta ya existente

  No crea una ruta nueva. Inicia sesión, abre el detalle que se pase por
  -Druta (URL o ID) y corre las mismas validaciones Backend ↔ Frontend
  (por lane, fila Total, Op cost visible y oculto). El reporte HTML se
  abre al terminar; si algo no cuadra, el final del reporte explica
  cada cálculo fallido con mucho detalle.

  Ejemplo:
    mvn test "-Dtest=RunnerCalculosRutaExistente" "-Druta=https://efdata-qa.efrouting.com/route-planner/detail/3417"

  @RutaExistente
  Scenario: Validar cálculos financieros de una ruta ya existente
    Given Usuario inicia sesión para validar la ruta existente
    When Usuario abre la ruta existente indicada
    And Usuario cierra el mapa para ver todas las columnas de la tabla
    And Usuario intercepta la respuesta financiera del endpoint user-route
    And Usuario valida los valores financieros de cada lane contra el Backend
    And Usuario valida los valores generales de la ruta contra el Backend
    And Usuario valida el comportamiento del Op cost oculto
    Then Usuario genera el reporte detallado de los cálculos financieros
