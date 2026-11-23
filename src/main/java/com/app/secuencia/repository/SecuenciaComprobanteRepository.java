package com.app.secuencia.repository;

import com.app.secuencia.model.SecuenciaComprobante;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@JdbcRepository(dialect = Dialect.H2)
public interface SecuenciaComprobanteRepository extends CrudRepository<SecuenciaComprobante, Long> {

    @Query("SELECT * FROM secuencia_comprobante WHERE tipo = :tipo FOR UPDATE")
    Optional<SecuenciaComprobante> findByTipoForUpdate(String tipo);
}
