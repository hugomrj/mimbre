package com.app.proveedor;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface ProveedorRepository extends CrudRepository<Proveedor, Long> {
    @Query("SELECT * FROM proveedor WHERE LOWER(nombre) LIKE LOWER(:pattern) OR LOWER(ruc) LIKE LOWER(:pattern)")
    List<Proveedor> buscar(String pattern);
}
