Feature: Validacion de la calculadora en Loadboard

  Login → Loadboard → Search loads → esperar 15 s a que carguen resultados →
  abrir carga con punto verde → validar texto verde en Load details →
  Calculate operation → Current profit (ciudades + %) → Income/Distance = RPM.
  Si falla una carga, cierra modales y prueba otra distinta.

  @Regression @ValidacionCalculadora
  Scenario: Validar Current profit en la calculadora de una carga
    Given Usuario se encuentra pantalla de inicio de sesión
    When Usuario ingresa credenciales de acceso
    And Usuario selecciona el botón "Log in"
    And Usuario se dirige a la sección "Loadboard"
    And Usuario debería visualizar la pantalla de Loadboard
    And Usuario busca cargas con origen y destino variables
    Then Usuario abre cargas hasta validar Current profit con ciudades y porcentaje
