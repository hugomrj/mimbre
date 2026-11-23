package com.app.compra.service;

import com.app.compra.dto.CompraDetalleDto;
import com.app.compra.dto.CompraDto;
import com.app.compra.model.Compra;
import com.app.compra.model.CompraDetalle;
import com.app.compra.repository.CompraDetalleRepository;
import com.app.compra.repository.CompraRepository;
import com.app.exception.BusinessException;
import com.app.exception.ResourceNotFoundException;
import com.app.movimiento_stock.service.MovimientoStockService;
import com.app.secuencia.service.SecuenciaComprobanteService;
import com.app.producto.model.Producto;
import com.app.producto.repository.ProductoRepository;
import com.app.proveedor.Proveedor;
import com.app.proveedor.ProveedorRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class CompraService {

    private final CompraRepository compraRepository;
    private final CompraDetalleRepository compraDetalleRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final MovimientoStockService movimientoStockService;
    private final SecuenciaComprobanteService secuenciaComprobanteService;

    public CompraService(CompraRepository compraRepository,
                         CompraDetalleRepository compraDetalleRepository,
                         ProductoRepository productoRepository,
                         ProveedorRepository proveedorRepository,
                         MovimientoStockService movimientoStockService,
                         SecuenciaComprobanteService secuenciaComprobanteService) {
        this.compraRepository = compraRepository;
        this.compraDetalleRepository = compraDetalleRepository;
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.movimientoStockService = movimientoStockService;
        this.secuenciaComprobanteService = secuenciaComprobanteService;
    }

    public List<CompraDto> findAll() {
        return StreamSupport.stream(compraRepository.findAllOrderByIdDesc().spliterator(), false)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<CompraDto> findById(Long id) {
        return compraRepository.findById(id).map(compra -> {
            CompraDto dto = mapToDto(compra);
            List<CompraDetalleDto> detalles = compraDetalleRepository.findByCompraId(compra.getId())
                    .stream()
                    .map(this::mapDetalleToDto)
                    .collect(Collectors.toList());
            dto.setDetalles(detalles);
            return dto;
        });
    }

    @Transactional
    public CompraDto registrarCompra(CompraDto dto) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new BusinessException("La compra debe contener al menos un producto.");
        }

        // Proveedor
        if (dto.getProveedorId() != null) {
            Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado (ID: " + dto.getProveedorId() + ")"));
            if (!proveedor.isActivo()) {
                throw new BusinessException("El proveedor '" + proveedor.getNombre() + "' esta inactivo.");
            }
            dto.setProveedorNombre(proveedor.getNombre());
        }

        Compra compra = new Compra();

        // Numero de factura autogenerado
        if (dto.getNumeroFactura() == null || dto.getNumeroFactura().trim().isEmpty()) {
            compra.setNumeroFactura(secuenciaComprobanteService.siguiente("COMPRA"));
        } else {
            compra.setNumeroFactura(dto.getNumeroFactura());
        }

        compra.setProveedorId(dto.getProveedorId());
        compra.setProveedorNombre(dto.getProveedorNombre() != null ? dto.getProveedorNombre() : "");
        compra.setFecha(dto.getFecha() != null ? dto.getFecha() : LocalDateTime.now());
        compra.setCondicion(dto.getCondicion() != null ? dto.getCondicion() : "Contado");
        compra.setEstado("COMPLETADA");

        BigDecimal total = BigDecimal.ZERO;
        compra.setMontoTotal(total);

        // Guardar compra inicial para obtener ID
        Compra savedCompra = compraRepository.save(compra);

        // Procesar detalles y AUMENTAR stock
        if (dto.getDetalles() != null) {
            for (CompraDetalleDto dDto : dto.getDetalles()) {
                CompraDetalle detalle = new CompraDetalle();
                detalle.setCompraId(savedCompra.getId());
                detalle.setProductoId(dDto.getProductoId());

                if (dDto.getProductoId() != null) {
                    Producto prod = productoRepository.findById(dDto.getProductoId())
                            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado (ID: " + dDto.getProductoId() + ")"));
                    if (!prod.isActivo()) {
                        throw new BusinessException("El producto '" + prod.getNombre() + "' esta inactivo.");
                    }
                    detalle.setProductoNombre(prod.getNombre());
                    detalle.setPrecioCosto(dDto.getPrecioCosto() != null ? dDto.getPrecioCosto() : prod.getPrecio());

                    // AUMENTAR STOCK Y RECALCULAR COSTO PROMEDIO
                    int cantidad = dDto.getCantidad() != null ? dDto.getCantidad() : 1;
                    detalle.setCantidad(cantidad);
                    BigDecimal costoCompra = detalle.getPrecioCosto();
                    BigDecimal nuevoCosto = costoPromedio(
                            prod.getStock() != null ? prod.getStock() : 0,
                            prod.getCostoActual(), cantidad, costoCompra);
                    prod.setCostoActual(nuevoCosto);
                    movimientoStockService.registrar(prod, cantidad, "COMPRA", "COMPRA",
                            savedCompra.getId(), "Compra " + savedCompra.getNumeroFactura(), costoCompra);
                } else {
                    detalle.setProductoNombre(dDto.getProductoNombre() != null ? dDto.getProductoNombre() : "Producto");
                    detalle.setPrecioCosto(dDto.getPrecioCosto() != null ? dDto.getPrecioCosto() : BigDecimal.ZERO);
                    detalle.setCantidad(dDto.getCantidad() != null ? dDto.getCantidad() : 1);
                }

                BigDecimal subtotal = detalle.getPrecioCosto().multiply(BigDecimal.valueOf(detalle.getCantidad()));
                detalle.setSubtotal(subtotal);
                total = total.add(subtotal);

                compraDetalleRepository.save(detalle);
            }
        }

        savedCompra.setMontoTotal(total);
        savedCompra = compraRepository.update(savedCompra);

        return findById(savedCompra.getId()).orElse(mapToDto(savedCompra));
    }

    @Transactional
    public boolean anular(Long id) {
        Optional<Compra> compraOpt = compraRepository.findById(id);
        if (compraOpt.isPresent() && !"ANULADA".equals(compraOpt.get().getEstado())) {
            Compra compra = compraOpt.get();
            compra.setEstado("ANULADA");
            compra.setFechaAnulacion(LocalDateTime.now());
            compraRepository.update(compra);

            // Reducir stock
            List<CompraDetalle> detalles = compraDetalleRepository.findByCompraId(compra.getId());
            for (CompraDetalle d : detalles) {
                if (d.getProductoId() != null) {
                    productoRepository.findById(d.getProductoId()).ifPresent(prod ->
                        movimientoStockService.registrar(prod, -d.getCantidad(), "ANULACION_COMPRA",
                                "ANULACION_COMPRA", compra.getId(),
                                "Anulación compra " + compra.getNumeroFactura(), prod.getCostoActual()));
                }
            }
            return true;
        }
        return false;
    }

    private BigDecimal costoPromedio(int stockAnterior, BigDecimal costoAnterior,
                                     int cantidad, BigDecimal costoCompra) {
        BigDecimal anterior = costoAnterior != null ? costoAnterior : BigDecimal.ZERO;
        BigDecimal compra = costoCompra != null ? costoCompra : BigDecimal.ZERO;
        int total = stockAnterior + cantidad;
        if (total <= 0) {
            return compra;
        }
        return anterior.multiply(BigDecimal.valueOf(stockAnterior))
                .add(compra.multiply(BigDecimal.valueOf(cantidad)))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private CompraDto mapToDto(Compra compra) {
        CompraDto dto = new CompraDto();
        dto.setId(compra.getId());
        dto.setNumeroFactura(compra.getNumeroFactura());
        dto.setProveedorId(compra.getProveedorId());
        dto.setProveedorNombre(compra.getProveedorNombre());
        dto.setFecha(compra.getFecha());
        dto.setCondicion(compra.getCondicion());
        dto.setMontoTotal(compra.getMontoTotal());
        dto.setEstado(compra.getEstado() != null && !compra.getEstado().trim().isEmpty() ? compra.getEstado() : "COMPLETADA");
        dto.setFechaAnulacion(compra.getFechaAnulacion());
        return dto;
    }

    private CompraDetalleDto mapDetalleToDto(CompraDetalle detalle) {
        CompraDetalleDto dto = new CompraDetalleDto();
        dto.setId(detalle.getId());
        dto.setCompraId(detalle.getCompraId());
        dto.setProductoId(detalle.getProductoId());
        dto.setProductoNombre(detalle.getProductoNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioCosto(detalle.getPrecioCosto());
        dto.setSubtotal(detalle.getSubtotal());
        return dto;
    }
}
