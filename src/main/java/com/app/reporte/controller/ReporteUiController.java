package com.app.reporte.controller;

import com.app.reporte.dto.ReporteDto;
import com.app.reporte.service.ReporteService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.views.View;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("/ui/reportes")
public class ReporteUiController {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ReporteService reporteService;

    public ReporteUiController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @View("reportes/hub")
    @Get
    public Map<String, Object> hub() {
        return Map.of();
    }

    @View("reportes/reporte")
    @Get("/{tipo}{?desde,hasta}")
    public Map<String, Object> reporte(@PathVariable String tipo,
                                       @Nullable @QueryValue("desde") String desde,
                                       @Nullable @QueryValue("hasta") String hasta) {
        LocalDate inicio = parse(desde, LocalDate.now().withDayOfMonth(1));
        LocalDate fin = parse(hasta, LocalDate.now());
        if (inicio.isAfter(fin)) {
            LocalDate tmp = inicio;
            inicio = fin;
            fin = tmp;
        }

        ReporteDto dto = reporteService.construir(tipo,
                inicio.atStartOfDay(), fin.atTime(23, 59, 59));

        Map<String, Object> model = new HashMap<>();
        model.put("reporte", dto);
        model.put("desde", inicio.format(ISO));
        model.put("hasta", fin.format(ISO));
        model.put("exportUrl", "/ui/reportes/exportar/" + tipo + "?desde=" + inicio.format(ISO)
                + "&hasta=" + fin.format(ISO));
        model.put("pdfUrl", "/ui/reportes/" + tipo + "/pdf?desde=" + inicio.format(ISO)
                + "&hasta=" + fin.format(ISO));
        return model;
    }

    @View("reportes/pdf")
    @Get("/{tipo}/pdf{?desde,hasta}")
    public Map<String, Object> pdf(@PathVariable String tipo,
                                   @Nullable @QueryValue("desde") String desde,
                                   @Nullable @QueryValue("hasta") String hasta) {
        LocalDate inicio = parse(desde, LocalDate.now().withDayOfMonth(1));
        LocalDate fin = parse(hasta, LocalDate.now());
        if (inicio.isAfter(fin)) {
            LocalDate tmp = inicio;
            inicio = fin;
            fin = tmp;
        }
        Map<String, Object> model = new HashMap<>();
        model.put("reporte", reporteService.construir(tipo,
                inicio.atStartOfDay(), fin.atTime(23, 59, 59)));
        model.put("desde", inicio.format(ISO));
        model.put("hasta", fin.format(ISO));
        return model;
    }

    @Get("/exportar/{tipo}{?desde,hasta}")
    public HttpResponse<byte[]> exportar(@PathVariable String tipo,
                                         @Nullable @QueryValue("desde") String desde,
                                         @Nullable @QueryValue("hasta") String hasta) {
        LocalDate inicio = parse(desde, LocalDate.now().withDayOfMonth(1));
        LocalDate fin = parse(hasta, LocalDate.now());
        if (inicio.isAfter(fin)) {
            LocalDate tmp = inicio;
            inicio = fin;
            fin = tmp;
        }

        ReporteDto dto = reporteService.construir(tipo,
                inicio.atStartOfDay(), fin.atTime(23, 59, 59));

        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append(String.join(";", dto.getColumnas())).append("\r\n");
        for (List<String> fila : dto.getFilas()) {
            csv.append(fila.stream().map(this::quote).reduce((a, b) -> a + ";" + b).orElse("")).append("\r\n");
        }

        String nombre = "reporte-" + tipo + "-" + inicio.format(ISO) + "-" + fin.format(ISO) + ".csv";
        return HttpResponse.ok(csv.toString().getBytes(StandardCharsets.UTF_8))
                .contentType(MediaType.of("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"");
    }

    private LocalDate parse(String valor, LocalDate porDefecto) {
        if (valor == null || valor.trim().isEmpty()) return porDefecto;
        try {
            return LocalDate.parse(valor.trim(), ISO);
        } catch (Exception e) {
            return porDefecto;
        }
    }

    private String quote(String valor) {
        if (valor == null) return "";
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }
}
