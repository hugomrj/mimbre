package com.app;

import com.app.producto.model.Producto;
import com.app.movimiento_stock.repository.MovimientoStockRepository;
import com.app.producto.repository.ProductoRepository;
import com.app.producto_categoria.repository.ProductoCategoriaRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(propertySources = "classpath:application-test.properties")
class SeederTest {

    @Inject
    DemoDataSeeder demoDataSeeder;

    @Inject
    ProductoRepository productoRepository;

    @Inject
    ProductoCategoriaRepository productoCategoriaRepository;

    @Inject
    MovimientoStockRepository movimientoStockRepository;

    @Test
    void elSeedCargaProductosConCategoriaId() {
        demoDataSeeder.seed();

        assertEquals(10, productoCategoriaRepository.count());
        assertEquals(49, productoRepository.count());

        assertEquals(49, movimientoStockRepository.count());

        for (Producto producto : productoRepository.findAll()) {
            assertTrue(producto.getCategoriaId() != null,
                    "Producto sin categoria_id: " + producto.getNombre());
            assertTrue(producto.getCostoActual() != null);
            assertTrue(producto.getStockMinimo() != null);
        }
    }
}
