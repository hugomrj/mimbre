package com.app;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;
import java.util.Map;

@Controller("/")
public class HomeController {

    @View("main") // Buscará src/main/resources/views/main.html
    @Get("/")
    public Map<String, Object> index() {
        return Map.of(
                "titulo", "FARO - Dashboard Principal"
        );
    }
}