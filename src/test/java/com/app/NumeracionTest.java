package com.app;

import com.app.compra.dto.CompraDetalleDto;
import com.app.compra.dto.CompraDto;
import com.app.compra.repository.CompraRepository;
import com.app.compra.service.CompraService;
import com.app.producto.dto.ProductoDto;
import com.app.producto.dto.ProductoFormDto;
import com.app.producto.service.ProductoService;
import com.app.producto_categoria.model.ProductoCategoria;
import com.app.producto_categoria.repository.ProductoCategoriaRepository;
import com.app.venta.dto.VentaDetalleDto;
import com.app.venta.dto.VentaDto;
import com.app.venta.repository.VentaRepository;
import com.app.venta.service.VentaService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(propertySources = "classpath:application-test.properties")
class NumeracionTest {

    @Inject
    ProductoService productoService;

    @Inject
    VentaService ventaService;

    @Inject
    CompraService compraService;

    @Inject
    VentaRepository ventaRepository;

    @Inject
    CompraRepository compraRepository;

    @Inject
    ProductoCategoriaRepository productoCategoriaRepository;

    @Test
    void laSecuenciaNoReutilizaNumerosAlBorrar() {
        ProductoDto producto = crearProducto();

        List<VentaDto> ventas = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ventas.add(ventaService.registrarVenta(venta(producto.getId())));
        }

        assertEquals("001-001-0000001", ventas.get(0).getNumeroFactura());
        assertEquals("001-001-0000002", ventas.get(1).getNumeroFactura());
        assertEquals("001-001-0000003", ventas.get(2).getNumeroFactura());

        // Se borra la venta del medio: el siguiente numero no debe reutilizarse.
        ventaRepository.deleteById(ventas.get(1).getId());

        VentaDto cuarta = ventaService.registrarVenta(venta(producto.getId()));
        assertEquals("001-001-0000004", cuarta.getNumeroFactura());
        assertTrue(ventaRepository.count() >= 3);
    }

    @Test
    void lasComprasUsanSuPropiaSecuencia() {
        ProductoDto producto = crearProducto();

        CompraDto primera = compraService.registrarCompra(compra(producto.getId()));
        CompraDto segunda = compraService.registrarCompra(compra(producto.getId()));

        assertEquals("COMP-0000001", primera.getNumeroFactura());
        assertEquals("COMP-0000002", segunda.getNumeroFactura());
        assertEquals(2, compraRepository.count());
    }

    private ProductoDto crearProducto() {
        ProductoCategoria categoria = productoCategoriaRepository.save(new ProductoCategoria("Num", "Num"));
        ProductoFormDto form = new ProductoFormDto("Producto numeracion", "SKU-NUM",
                new BigDecimal("100"), 100, "Num");
        form.setCategoriaId(categoria.getId());
        form.setCostoActual(new BigDecimal("50"));
        form.setStockMinimo(5);
        return productoService.save(form);
    }

    private VentaDto venta(Long productoId) {
        VentaDto venta = new VentaDto();
        VentaDetalleDto detalle = new VentaDetalleDto();
        detalle.setProductoId(productoId);
        detalle.setCantidad(1);
        venta.setDetalles(List.of(detalle));
        return venta;
    }

    private CompraDto compra(Long productoId) {
        CompraDto compra = new CompraDto();
        CompraDetalleDto detalle = new CompraDetalleDto();
        detalle.setProductoId(productoId);
        detalle.setCantidad(1);
        detalle.setPrecioCosto(new BigDecimal("60"));
        compra.setDetalles(List.of(detalle));
        return compra;
    }
}
