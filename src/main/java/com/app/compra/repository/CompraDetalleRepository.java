package com.app.compra.repository;

import com.app.compra.model.CompraDetalle;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface CompraDetalleRepository extends CrudRepository<CompraDetalle, Long> {
    List<CompraDetalle> findByCompraId(Long compraId);
}
