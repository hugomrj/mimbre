package com.app.movimiento_stock.repository;

import com.app.movimiento_stock.model.MovimientoStock;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface MovimientoStockRepository extends CrudRepository<MovimientoStock, Long> {

    List<MovimientoStock> findByProductoIdOrderByIdDesc(Long productoId);

    @Query("SELECT * FROM movimiento_stock ORDER BY id DESC")
    List<MovimientoStock> findAllOrdenado();
}
