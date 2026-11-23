package com.app;

import com.app.compra.dto.CompraDetalleDto;
import com.app.compra.dto.CompraDto;
import com.app.compra.service.CompraService;
import com.app.movimiento_stock.model.MovimientoStock;
import com.app.movimiento_stock.repository.MovimientoStockRepository;
import com.app.producto.dto.ProductoDto;
import com.app.producto.dto.ProductoFormDto;
import com.app.producto.model.Producto;
import com.app.producto.repository.ProductoRepository;
import com.app.producto.service.ProductoService;
import com.app.producto_categoria.model.ProductoCategoria;
import com.app.producto_categoria.repository.ProductoCategoriaRepository;
import com.app.venta.dto.VentaDetalleDto;
import com.app.venta.dto.VentaDto;
import com.app.venta.service.VentaService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(propertySources = "classpath:application-test.properties")
class KardexTest {

    @Inject
    ProductoService productoService;

    @Inject
    VentaService ventaService;

    @Inject
    CompraService compraService;

    @Inject
    ProductoRepository productoRepository;

    @Inject
    ProductoCategoriaRepository productoCategoriaRepository;

    @Inject
    MovimientoStockRepository movimientoStockRepository;

    @Test
    void ventaCompraYAnulacionDejanKardexConsistente() {
        ProductoCategoria categoria = productoCategoriaRepository.save(new ProductoCategoria("Test", "Test"));
        ProductoFormDto form = new ProductoFormDto("Kardex", "SKU-KARDEX",
                new BigDecimal("200"), 10, "Test");
        form.setCategoriaId(categoria.getId());
        form.setCostoActual(new BigDecimal("100"));
        form.setStockMinimo(3);

        ProductoDto producto = productoService.save(form);

        // Venta de 3: 10 -> 7
        VentaDto venta = new VentaDto();
        VentaDetalleDto detalleVenta = new VentaDetalleDto();
        detalleVenta.setProductoId(producto.getId());
        detalleVenta.setCantidad(3);
        venta.setDetalles(List.of(detalleVenta));
        VentaDto ventaGuardada = ventaService.registrarVenta(venta);

        assertEquals(7, stock(producto.getId()));

        // Compra de 5 a 120: costo (7*100 + 5*120)/12 = 108.33
        CompraDto compra = new CompraDto();
        CompraDetalleDto detalleCompra = new CompraDetalleDto();
        detalleCompra.setProductoId(producto.getId());
        detalleCompra.setCantidad(5);
        detalleCompra.setPrecioCosto(new BigDecimal("120"));
        compra.setDetalles(List.of(detalleCompra));
        compraService.registrarCompra(compra);

        assertEquals(12, stock(producto.getId()));
        Producto actualizado = productoRepository.findById(producto.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("108.33").compareTo(actualizado.getCostoActual()));

        // Anular la venta: 12 -> 15
        assertTrue(ventaService.anular(ventaGuardada.getId()));
        assertEquals(15, stock(producto.getId()));

        List<MovimientoStock> movimientos =
                movimientoStockRepository.findByProductoIdOrderByIdDesc(producto.getId());
        int suma = movimientos.stream().mapToInt(MovimientoStock::getCantidad).sum();
        assertEquals(15, suma);
        assertTrue(movimientos.stream().anyMatch(m -> "VENTA".equals(m.getTipo())));
        assertTrue(movimientos.stream().anyMatch(m -> "COMPRA".equals(m.getTipo())));
        assertTrue(movimientos.stream().anyMatch(m -> "ANULACION_VENTA".equals(m.getTipo())));
    }

    private int stock(Long productoId) {
        return productoRepository.findById(productoId).orElseThrow().getStock();
    }
}
