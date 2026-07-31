package com.app.venta;

import com.app.cliente.Cliente;
import com.app.cliente.ClienteRepository;
import com.app.producto.Producto;
import com.app.producto.ProductoRepository;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class VentaService {

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;

    public VentaService(VentaRepository ventaRepository,
                        VentaDetalleRepository ventaDetalleRepository,
                        ProductoRepository productoRepository,
                        ClienteRepository clienteRepository) {
        this.ventaRepository = ventaRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
    }

    public List<VentaDto> findAll() {
        return StreamSupport.stream(ventaRepository.findAll().spliterator(), false)
                .map(this::mapToDto)
                .collect(Collectors.toList());
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

    public VentaDto registrarVenta(VentaDto dto) {
        Venta venta = new Venta();
        
        // Número de factura autogenerado
        if (dto.getNumeroFactura() == null || dto.getNumeroFactura().trim().isEmpty()) {
            long count = ventaRepository.count() + 1;
            venta.setNumeroFactura(String.format("001-001-%07d", count));
        } else {
            venta.setNumeroFactura(dto.getNumeroFactura());
        }

        // Cliente
        venta.setClienteId(dto.getClienteId());
        if (dto.getClienteId() != null) {
            clienteRepository.findById(dto.getClienteId())
                    .ifPresent(cli -> venta.setClienteNombre(cli.getNombre()));
        } else {
            venta.setClienteNombre(dto.getClienteNombre() != null ? dto.getClienteNombre() : "Cliente Ocasional");
        }

        venta.setFecha(dto.getFecha() != null ? dto.getFecha() : LocalDateTime.now());
        venta.setCondicion(dto.getCondicion() != null ? dto.getCondicion() : "Contado");
        venta.setEstado("COMPLETADA");

        BigDecimal total = BigDecimal.ZERO;

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
                    prod.setStock(Math.max(0, prod.getStock() - cantidad));
                    productoRepository.update(prod);
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

    public boolean anular(Long id) {
        Optional<Venta> ventaOpt = ventaRepository.findById(id);
        if (ventaOpt.isPresent() && !"ANULADA".equals(ventaOpt.get().getEstado())) {
            Venta venta = ventaOpt.get();
            venta.setEstado("ANULADA");
            ventaRepository.update(venta);

            // Reponer stock
            List<VentaDetalle> detalles = ventaDetalleRepository.findByVentaId(venta.getId());
            for (VentaDetalle d : detalles) {
                if (d.getProductoId() != null) {
                    productoRepository.findById(d.getProductoId()).ifPresent(prod -> {
                        prod.setStock(prod.getStock() + d.getCantidad());
                        productoRepository.update(prod);
                    });
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
        dto.setEstado(venta.getEstado());
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
