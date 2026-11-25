package com.app;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class BackupDto {
    private final String archivo;
    private final String fecha;
    private final String tamano;

    public BackupDto(String archivo, String fecha, String tamano) {
        this.archivo = archivo;
        this.fecha = fecha;
        this.tamano = tamano;
    }

    public String getArchivo() { return archivo; }
    public String getFecha() { return fecha; }
    public String getTamano() { return tamano; }
}
