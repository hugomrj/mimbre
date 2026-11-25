package com.app.reporte.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Serdeable
public class ReporteDto {
    private String tipo;
    private String titulo;
    private String subtitulo;
    private boolean mostrarFechas;
    private List<String> columnas = new ArrayList<>();
    private List<List<String>> filas = new ArrayList<>();
    private List<Map<String, String>> kpis = new ArrayList<>();

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getSubtitulo() { return subtitulo; }
    public void setSubtitulo(String subtitulo) { this.subtitulo = subtitulo; }

    public boolean isMostrarFechas() { return mostrarFechas; }
    public void setMostrarFechas(boolean mostrarFechas) { this.mostrarFechas = mostrarFechas; }

    public List<String> getColumnas() { return columnas; }
    public void setColumnas(List<String> columnas) { this.columnas = columnas; }

    public List<List<String>> getFilas() { return filas; }
    public void setFilas(List<List<String>> filas) { this.filas = filas; }

    public List<Map<String, String>> getKpis() { return kpis; }
    public void setKpis(List<Map<String, String>> kpis) { this.kpis = kpis; }

    public void kpi(String etiqueta, String valor) {
        kpis.add(Map.of("etiqueta", etiqueta, "valor", valor));
    }

    public void fila(String... valores) {
        filas.add(List.of(valores));
    }
}
