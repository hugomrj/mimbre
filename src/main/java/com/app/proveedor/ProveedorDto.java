package com.app.proveedor;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Pattern;

@Serdeable
public class ProveedorDto {
    private Long id;

    private String nombre;

    private String ruc;

    private String telefono;

    @Pattern(
            regexp = "^\\s*$|^\\s*[^@\\s]+@[^@\\s]+\\s*$",
            message = "El formato del correo electrónico no es válido"
    )
    private String email;

    private String direccion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = (email == null || email.isBlank()) ? null : email.trim();
    }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
