package com.app.movimiento_stock.service;

import com.app.exception.StockInsuficienteException;
import com.app.movimiento_stock.dto.MovimientoStockDto;
import com.app.movimiento_stock.model.MovimientoStock;
import com.app.movimiento_stock.repository.MovimientoStockRepository;
import com.app.producto.model.Producto;
import com.app.producto.repository.ProductoRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Unico punto de escritura del stock: actualiza producto.stock y deja la fila
 * correspondiente en movimiento_stock, siempre dentro de la misma transaccion.
 */
@Singleton
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoRepository;
    private final ProductoRepository productoRepository;

    public MovimientoStockService(MovimientoStockRepository movimientoRepository,
                                  ProductoRepository productoRepository) {
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public MovimientoStock registrar(Producto producto,
                                     int cantidad,
                                     String tipo,
                                     String origenTipo,
                                     Long origenId,
                                     String motivo,
                                     BigDecimal costoUnitario) {
        if (cantidad == 0) {
            return null;
        }

        int anterior = producto.getStock() != null ? producto.getStock() : 0;
        int nuevo = anterior + cantidad;
        if (nuevo < 0) {
            throw new StockInsuficienteException("Stock insuficiente para '" + producto.getNombre()
                    + "'. Disponible: " + anterior + ", solicitado: " + Math.abs(cantidad) + ".");
        }

        producto.setStock(nuevo);
        productoRepository.update(producto);

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProductoId(producto.getId());
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(anterior);
        movimiento.setStockNuevo(nuevo);
        movimiento.setCostoUnitario(costoUnitario != null ? costoUnitario : producto.getCostoActual());
        movimiento.setOrigenTipo(origenTipo);
        movimiento.setOrigenId(origenId);
        movimiento.setMotivo(motivo);

        return movimientoRepository.save(movimiento);
    }

    public List<MovimientoStockDto> findAll() {
        Map<Long, String> nombres = new HashMap<>();
        for (Producto producto : productoRepository.findAll()) {
            nombres.put(producto.getId(), producto.getNombre());
        }
        return movimientoRepository.findAllOrdenado().stream()
                .map(movimiento -> {
                    MovimientoStockDto dto = mapToDto(movimiento);
                    dto.setProductoNombre(nombres.getOrDefault(movimiento.getProductoId(), "Producto"));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<MovimientoStockDto> findByProducto(Long productoId) {
        if (productoId == null) {
            return List.of();
        }
        return movimientoRepository.findByProductoIdOrderByIdDesc(productoId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private MovimientoStockDto mapToDto(MovimientoStock movimiento) {
        MovimientoStockDto dto = new MovimientoStockDto();
        dto.setId(movimiento.getId());
        dto.setProductoId(movimiento.getProductoId());
        dto.setTipo(movimiento.getTipo());
        dto.setCantidad(movimiento.getCantidad());
        dto.setStockAnterior(movimiento.getStockAnterior());
        dto.setStockNuevo(movimiento.getStockNuevo());
        dto.setCostoUnitario(movimiento.getCostoUnitario());
        dto.setOrigenTipo(movimiento.getOrigenTipo());
        dto.setOrigenId(movimiento.getOrigenId());
        dto.setMotivo(movimiento.getMotivo());
        dto.setCreadoEn(movimiento.getCreadoEn());
        return dto;
    }
}
