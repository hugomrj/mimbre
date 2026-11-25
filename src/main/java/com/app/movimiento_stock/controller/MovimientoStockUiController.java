package com.app.movimiento_stock.controller;

import com.app.movimiento_stock.dto.MovimientoStockDto;
import com.app.movimiento_stock.service.MovimientoStockService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.views.View;

import java.util.List;
import java.util.Map;

@Controller("/ui/movimientos")
public class MovimientoStockUiController {

    private final MovimientoStockService movimientoStockService;

    public MovimientoStockUiController(MovimientoStockService movimientoStockService) {
        this.movimientoStockService = movimientoStockService;
    }

    @View("movimiento_stock/table")
    @Get
    public Map<String, Object> list(@Nullable @QueryValue("tipo") String tipo,
                                    @Nullable @QueryValue("q") String q) {
        List<MovimientoStockDto> movimientos = movimientoStockService.findAll();

        if (tipo != null && !tipo.trim().isEmpty() && !"TODOS".equalsIgnoreCase(tipo)) {
            movimientos = movimientos.stream()
                    .filter(m -> tipo.equalsIgnoreCase(m.getTipo()))
                    .toList();
        }

        if (q != null && !q.trim().isEmpty()) {
            String texto = q.trim().toLowerCase();
            movimientos = movimientos.stream()
                    .filter(m -> coincide(m.getProductoNombre(), texto)
                            || coincide(m.getTipo(), texto)
                            || coincide(m.getMotivo(), texto)
                            || coincide(m.getOrigenTipo(), texto))
                    .toList();
        }

        String tipoFiltro = tipo == null || tipo.trim().isEmpty() ? "TODOS" : tipo.toUpperCase();
        return Map.of(
                "movimientos", movimientos,
                "tipoFiltro", tipoFiltro,
                "q", q != null ? q : ""
        );
    }

    private boolean coincide(String valor, String texto) {
        return valor != null && valor.toLowerCase().contains(texto);
    }
}
