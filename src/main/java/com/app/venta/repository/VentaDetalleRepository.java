package com.app.venta.repository;

import com.app.venta.model.VentaDetalle;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface VentaDetalleRepository extends CrudRepository<VentaDetalle, Long> {
    List<VentaDetalle> findByVentaId(Long ventaId);
    long countByProductoId(Long productoId);
}
