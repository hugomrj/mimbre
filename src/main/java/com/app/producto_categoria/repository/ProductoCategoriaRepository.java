package com.app.producto_categoria.repository;

import com.app.producto_categoria.model.ProductoCategoria;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.PageableRepository;

@JdbcRepository(dialect = Dialect.H2)
public interface ProductoCategoriaRepository extends PageableRepository<ProductoCategoria, Long> {
}