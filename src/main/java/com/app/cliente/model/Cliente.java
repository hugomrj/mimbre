package com.app.cliente.model;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

@MappedEntity
public class Cliente {

    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

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
}
