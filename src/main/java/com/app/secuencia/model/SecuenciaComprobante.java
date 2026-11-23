package com.app.secuencia.model;

import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.time.LocalDateTime;

@MappedEntity
public class SecuenciaComprobante {

    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

    private String tipo;
    private String prefijo;
    private Long ultimoNumero;
    private Integer longitud;

    @DateCreated
    private LocalDateTime creadoEn;

    @DateUpdated
    private LocalDateTime actualizadoEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getPrefijo() { return prefijo; }
    public void setPrefijo(String prefijo) { this.prefijo = prefijo; }

    public Long getUltimoNumero() { return ultimoNumero; }
    public void setUltimoNumero(Long ultimoNumero) { this.ultimoNumero = ultimoNumero; }

    public Integer getLongitud() { return longitud; }
    public void setLongitud(Integer longitud) { this.longitud = longitud; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
