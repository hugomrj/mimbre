package com.app.dashboard;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;
import java.util.Map;

@Controller("/ui/dashboard")
public class DashboardUiController {

    @View("dashboard")
    @Get
    public Map<String, Object> index() {
        // En el futuro aquí se inyectarán servicios para enviar datos reales
        return Map.of();
    }
}
