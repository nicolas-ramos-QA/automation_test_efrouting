package com.filtros_de_rutas.models;

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

    public String getRoutePlannerUrl() {
        if (url != null && url.contains("/login")) {
            return url.replace("/login", "/route-planner");
        }
        return "https://efdata-qa.efrouting.com/route-planner";
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

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
