Feature: Flujo de creacion de ruta y verificacion de millas en linea

  @Regression @FlujoCreaciónRuta
  Scenario: Creación de una nueva ruta por tipo de tráiler
    Given Usuario se encuentra pantalla de inicio de sesión
    When Usuario ingresa credenciales de acceso
    And Usuario selecciona el botón "Log in"
    And Usuario se dirige a la sección "Routes"
    And Usuario selecciona el botón "New route"
    And Usuario selecciona la opción "Trailer type only"
    And Usuario completa los datos de la nueva ruta
    And Usuario finaliza la creación seleccionando "Continue"
    And Usuario selecciona la segunda ruta de "Tri-hauls"
    And Usuario edita la última línea de la ruta
    Then Usuario verifica que las millas de la ruta sean correctas
