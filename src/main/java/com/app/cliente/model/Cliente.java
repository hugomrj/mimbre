package com.app.cliente.model;

import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import java.time.LocalDateTime;

@MappedEntity
public class Cliente {

    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

    @DateCreated
    private LocalDateTime creadoEn;

    @DateUpdated
    private LocalDateTime actualizadoEn;

    private boolean activo = true;

    private String nombre;
    private String rucDocumento;
    private String telefono;
    private String email;
    private String direccion;

    public Cliente() {
    }

    public Cliente(String nombre, String rucDocumento, String telefono, String email, String direccion) {
        this.nombre = nombre;
        this.rucDocumento = rucDocumento;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRucDocumento() { return rucDocumento; }
    public void setRucDocumento(String rucDocumento) { this.rucDocumento = rucDocumento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
