package com.app.compra.repository;

import com.app.compra.model.Compra;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface CompraRepository extends CrudRepository<Compra, Long> {

    List<Compra> findAllOrderByIdDesc();
    long countByProveedorId(Long proveedorId);
}
