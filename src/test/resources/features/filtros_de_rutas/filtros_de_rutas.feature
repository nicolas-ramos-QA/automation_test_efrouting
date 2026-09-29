Feature: Validacion de filtros del listado de rutas

  Solo QA: https://efdata-qa.efrouting.com/route-planner

  Login → Routes → leer los totales del pie (Total routes, Total income,
  Total miles, DH miles, Effective RPM, Loaded RPM) → abrir el panel Filter
  y aplicar los 12 filtros (Route name, Start date, End date, Origin,
  Destination, Driver, Trailer, Unit, Dispatcher, Equipment type, Total miles,
  Income) con un valor de la primera fila. Tras cada filtro los totales del
  pie deben cambiar. Luego Reset y se comprueba que vuelven. Genera reporte HTML.

  @Regression @FiltrosDeRutas
  Scenario: Aplicar cada filtro de rutas y verificar que cambian los totales del pie
    Given Usuario se encuentra pantalla de inicio de sesión
    When Usuario ingresa credenciales de acceso
    And Usuario selecciona el botón "Log in"
    And Usuario se dirige a la sección "Routes"
    And Usuario debería visualizar el listado de rutas
    Then Usuario aplica cada filtro y verifica que los totales del pie cambian
