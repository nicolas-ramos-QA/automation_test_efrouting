package com.calculos_de_rutas.models;

/**
 * Ambiente contra el que se ejecuta el escenario: su URL de login y sus credenciales.
 */
public class TestEnvironment {

    private final String name;
    private final String url;
    private final String email;
    private final String password;

    public TestEnvironment(String name, String url, String email, String password) {
        this.name = name;
        this.url = url;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    /** Nombre apto para nombres de archivo del reporte. */
    public String getSlug() {
        return name.toLowerCase()
                .replace("ó", "o")
                .replaceAll("[^a-z0-9]+", "-");
    }

    @Override
    public String toString() {
        return name + " (" + url + ")";
    }
}
