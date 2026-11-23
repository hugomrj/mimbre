package com.app.venta.service;

import com.app.cliente.repository.ClienteRepository;
import com.app.exception.BusinessException;
import com.app.exception.ResourceNotFoundException;
import com.app.exception.StockInsuficienteException;
import com.app.movimiento_stock.service.MovimientoStockService;
import com.app.secuencia.service.SecuenciaComprobanteService;
import com.app.producto.model.Producto;
import com.app.producto.repository.ProductoRepository;
import com.app.venta.model.VentaDetalle;
import com.app.venta.repository.VentaDetalleRepository;
import com.app.venta.repository.VentaRepository;
import com.app.venta.dto.VentaDetalleDto;
import com.app.venta.dto.VentaDto;
import com.app.venta.model.Venta;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class VentaService {

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final MovimientoStockService movimientoStockService;
    private final SecuenciaComprobanteService secuenciaComprobanteService;

    public VentaService(VentaRepository ventaRepository,
                        VentaDetalleRepository ventaDetalleRepository,
                        ProductoRepository productoRepository,
                        ClienteRepository clienteRepository,
                        MovimientoStockService movimientoStockService,
                        SecuenciaComprobanteService secuenciaComprobanteService) {
        this.ventaRepository = ventaRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.movimientoStockService = movimientoStockService;
        this.secuenciaComprobanteService = secuenciaComprobanteService;
    }

    public List<VentaDto> findAll() {
        return StreamSupport.stream(ventaRepository.findAllOrderByIdDesc().spliterator(), false)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<VentaDto> findAllConDetalles() {
        List<VentaDto> ventas = findAll();
        Map<Long, List<VentaDetalleDto>> detallesPorVenta = StreamSupport
                .stream(ventaDetalleRepository.findAll().spliterator(), false)
                .filter(detalle -> detalle.getVentaId() != null)
                .collect(Collectors.groupingBy(
                        VentaDetalle::getVentaId,
                        Collectors.mapping(this::mapDetalleToDto, Collectors.toList())));
        ventas.forEach(venta -> venta.setDetalles(detallesPorVenta.getOrDefault(venta.getId(), List.of())));
        return ventas;
    }

    public Optional<VentaDto> findById(Long id) {
        return ventaRepository.findById(id).map(venta -> {
            VentaDto dto = mapToDto(venta);
            List<VentaDetalleDto> detalles = ventaDetalleRepository.findByVentaId(venta.getId())
                    .stream()
                    .map(this::mapDetalleToDto)
                    .collect(Collectors.toList());
            dto.setDetalles(detalles);
            return dto;
        });
    }

    @Transactional
    public VentaDto registrarVenta(VentaDto dto) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new BusinessException("La venta debe contener al menos un producto.");
        }

        // Validar stock antes de realizar cambios
        for (VentaDetalleDto dDto : dto.getDetalles()) {
            if (dDto.getProductoId() != null) {
                Producto prod = productoRepository.findById(dDto.getProductoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado (ID: " + dDto.getProductoId() + ")"));
                int cantidad = dDto.getCantidad() != null ? dDto.getCantidad() : 1;
                if (!prod.isActivo()) {
                    throw new BusinessException("El producto '" + prod.getNombre() + "' esta inactivo y no se puede vender.");
                }
                if (prod.getStock() < cantidad) {
                    throw new StockInsuficienteException("Stock insuficiente para '" + prod.getNombre() + "'. Stock disponible: " + prod.getStock() + ", solicitado: " + cantidad);
                }
            }
        }

        Venta venta = new Venta();
        
        // Número de factura autogenerado
        if (dto.getNumeroFactura() == null || dto.getNumeroFactura().trim().isEmpty()) {
            venta.setNumeroFactura(secuenciaComprobanteService.siguiente("VENTA"));
        } else {
            venta.setNumeroFactura(dto.getNumeroFactura());
        }

        // Cliente
        venta.setClienteId(dto.getClienteId());
        if (dto.getClienteId() != null) {
            clienteRepository.findById(dto.getClienteId())
                    .ifPresent(cli -> {
                        if (!cli.isActivo()) {
                            throw new BusinessException("El cliente '" + cli.getNombre() + "' esta inactivo.");
                        }
                        venta.setClienteNombre(cli.getNombre());
                    });
        } else {
            venta.setClienteNombre(dto.getClienteNombre() != null ? dto.getClienteNombre() : "Cliente Ocasional");
        }

        venta.setFecha(dto.getFecha() != null ? dto.getFecha() : LocalDateTime.now());
        venta.setCondicion(dto.getCondicion() != null ? dto.getCondicion() : "Contado");
        venta.setEstado("COMPLETADA");

        BigDecimal total = BigDecimal.ZERO;
        venta.setMontoTotal(total);

        // Guardar venta inicial para obtener ID
        Venta savedVenta = ventaRepository.save(venta);

        // Procesar detalles y descontar stock
        if (dto.getDetalles() != null) {
            for (VentaDetalleDto dDto : dto.getDetalles()) {
                VentaDetalle detalle = new VentaDetalle();
                detalle.setVentaId(savedVenta.getId());
                detalle.setProductoId(dDto.getProductoId());

                Optional<Producto> prodOpt = dDto.getProductoId() != null 
                        ? productoRepository.findById(dDto.getProductoId()) 
                        : Optional.empty();

                if (prodOpt.isPresent()) {
                    Producto prod = prodOpt.get();
                    detalle.setProductoNombre(prod.getNombre());
                    detalle.setPrecioUnitario(dDto.getPrecioUnitario() != null ? dDto.getPrecioUnitario() : prod.getPrecio());
                    
                    // Descontar stock
                    int cantidad = dDto.getCantidad() != null ? dDto.getCantidad() : 1;
                    detalle.setCantidad(cantidad);
                    movimientoStockService.registrar(prod, -cantidad, "VENTA", "VENTA",
                            savedVenta.getId(), "Venta " + savedVenta.getNumeroFactura(), prod.getCostoActual());
                } else {
                    detalle.setProductoNombre(dDto.getProductoNombre() != null ? dDto.getProductoNombre() : "Producto");
                    detalle.setPrecioUnitario(dDto.getPrecioUnitario() != null ? dDto.getPrecioUnitario() : BigDecimal.ZERO);
                    detalle.setCantidad(dDto.getCantidad() != null ? dDto.getCantidad() : 1);
                }

                BigDecimal subtotal = detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad()));
                detalle.setSubtotal(subtotal);
                total = total.add(subtotal);

                ventaDetalleRepository.save(detalle);
            }
        }

        savedVenta.setMontoTotal(total);
        savedVenta = ventaRepository.update(savedVenta);

        return findById(savedVenta.getId()).orElse(mapToDto(savedVenta));
    }

    @Transactional
    public boolean anular(Long id) {
        Optional<Venta> ventaOpt = ventaRepository.findById(id);
        if (ventaOpt.isPresent() && !"ANULADA".equals(ventaOpt.get().getEstado())) {
            Venta venta = ventaOpt.get();
            venta.setEstado("ANULADA");
            venta.setFechaAnulacion(LocalDateTime.now());
            ventaRepository.update(venta);

            // Reponer stock
            List<VentaDetalle> detalles = ventaDetalleRepository.findByVentaId(venta.getId());
            for (VentaDetalle d : detalles) {
                if (d.getProductoId() != null) {
                    productoRepository.findById(d.getProductoId()).ifPresent(prod ->
                        movimientoStockService.registrar(prod, d.getCantidad(), "ANULACION_VENTA",
                                "ANULACION_VENTA", venta.getId(),
                                "Anulación venta " + venta.getNumeroFactura(), prod.getCostoActual()));
                }
            }
            return true;
        }
        return false;
    }

    private VentaDto mapToDto(Venta venta) {
        VentaDto dto = new VentaDto();
        dto.setId(venta.getId());
        dto.setNumeroFactura(venta.getNumeroFactura());
        dto.setClienteId(venta.getClienteId());
        dto.setClienteNombre(venta.getClienteNombre());
        dto.setFecha(venta.getFecha());
        dto.setCondicion(venta.getCondicion());
        dto.setMontoTotal(venta.getMontoTotal());
        dto.setEstado(venta.getEstado() != null && !venta.getEstado().trim().isEmpty() ? venta.getEstado() : "COMPLETADA");
        dto.setFechaAnulacion(venta.getFechaAnulacion());
        return dto;
    }

    private VentaDetalleDto mapDetalleToDto(VentaDetalle detalle) {
        VentaDetalleDto dto = new VentaDetalleDto();
        dto.setId(detalle.getId());
        dto.setVentaId(detalle.getVentaId());
        dto.setProductoId(detalle.getProductoId());
        dto.setProductoNombre(detalle.getProductoNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubtotal(detalle.getSubtotal());
        return dto;
    }
}
