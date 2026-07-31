package com.app.cliente;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface ClienteRepository extends CrudRepository<Cliente, Long> {

    @Query("SELECT * FROM cliente WHERE LOWER(nombre) LIKE LOWER(:q) OR LOWER(ruc_documento) LIKE LOWER(:q)")
    List<Cliente> buscar(String q);
}
