package com.app.dashboard.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;

import java.util.Map;

@Controller("/ui/dashboard")
public class DashboardUiController {

    @View("dashboard")
    @Get
    public Map<String, Object> index() {
        return Map.of();
    }
}
