Feature: Flujo de creacion de ruta y verificacion de millas en linea

  Se cubren cuatro casos al crear la ruta, forzando cada tipo de ruta sugerida:
  Tri-hauls, Bi-hauls, Best Choice y Loops.

  Si tras crear la ruta ese tipo no aparece entre las sugerencias (como en Easy
  routes cuando solo hay Best Choice y Loops), el escenario se anula (omitido)
  y no se marca como fallido. Si el tipo sí está disponible, el flujo continúa
  con edición de lane y verificación de millas.

  @Regression @FlujoCreaciónRuta
  Scenario Outline: Crear ruta tipo <tipoRuta> y verificar millas en línea
    Given Usuario se encuentra pantalla de inicio de sesión
    When Usuario ingresa credenciales de acceso
    And Usuario selecciona el botón "Log in"
    And Usuario se dirige a la sección "Routes"
    And Usuario selecciona el botón "New route"
    And Usuario selecciona la opción "Trailer type only"
    And Usuario completa los datos de la nueva ruta
    And Usuario finaliza la creación seleccionando "Continue"
    And Usuario selecciona una ruta sugerida de tipo "<tipoRuta>"
    And Usuario edita la última línea de la ruta
    Then Usuario verifica que las millas de la ruta sean correctas

    Examples:
      | tipoRuta    |
      | Tri-hauls   |
      | Bi-hauls    |
      | Best Choice |
      | Loops       |
