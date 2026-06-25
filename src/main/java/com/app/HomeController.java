package com.app;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;
import java.util.Map;

@Controller("/")
public class HomeController {

    @View("prueba") // Buscará src/main/resources/views/prueba.html
    @Get
    public Map<String, Object> index() {
        return Map.of(
                "titulo", "Prueba de Mimbre",
                "mensaje", "Hola! El template Pebble está funcionando correctamente."
        );
    }
}