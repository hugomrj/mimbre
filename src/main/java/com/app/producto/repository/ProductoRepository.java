package com.app.producto.repository;

import com.app.producto.model.Producto;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.PageableRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface ProductoRepository extends PageableRepository<Producto, Long> {

    @Query("SELECT * FROM producto WHERE activo = TRUE AND (LOWER(nombre) LIKE LOWER(:q) OR LOWER(sku) LIKE LOWER(:q))")
    List<Producto> buscar(String q);

    List<Producto> findAllByActivoTrue();

    Page<Producto> findAllByActivoTrue(Pageable pageable);

    long countByCategoria(String categoria);

    long countByCategoriaId(Long categoriaId);
}