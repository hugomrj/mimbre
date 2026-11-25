package com.app.producto_categoria.repository;

import com.app.producto_categoria.model.ProductoCategoria;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.PageableRepository;

import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.H2)
public interface ProductoCategoriaRepository extends PageableRepository<ProductoCategoria, Long> {

    List<ProductoCategoria> findAllByActivoTrue();

    Page<ProductoCategoria> findAllByActivoTrue(Pageable pageable);

    Optional<ProductoCategoria> findFirstByNombreIgnoreCase(String nombre);

    @Query(value = "SELECT * FROM producto_categoria WHERE activo = TRUE AND (LOWER(nombre) LIKE LOWER(:q) OR LOWER(descripcion) LIKE LOWER(:q)) ORDER BY id DESC",
            countQuery = "SELECT COUNT(*) FROM producto_categoria WHERE activo = TRUE AND (LOWER(nombre) LIKE LOWER(:q) OR LOWER(descripcion) LIKE LOWER(:q))")
    Page<ProductoCategoria> buscarPaginado(String q, Pageable pageable);
}