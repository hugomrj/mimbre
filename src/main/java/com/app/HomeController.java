package com.app;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;
import java.util.Map;

@Controller("/")
public class HomeController {

    @View("dashboard") // ✅ Ahora apunta al dashboard
    @Get("/")
    public Map<String, Object> index() {
        return Map.of(); // Ya no necesitamos el título "FARO", el layout lo maneja
    }
}