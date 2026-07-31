package com.app.producto.repository;

import com.app.producto.model.Producto;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface ProductoRepository extends CrudRepository<Producto, Long> {

    @Query("SELECT * FROM producto WHERE LOWER(nombre) LIKE LOWER(:q) OR LOWER(sku) LIKE LOWER(:q)")
    List<Producto> buscar(String q);

    long countByCategoria(String categoria);
}
