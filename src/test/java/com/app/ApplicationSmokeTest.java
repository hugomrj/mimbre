package com.app;

import com.app.producto.repository.ProductoRepository;
import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(propertySources = "classpath:application-test.properties")
class ApplicationSmokeTest {

    @Inject
    ApplicationContext context;

    @Inject
    ProductoRepository productoRepository;

    @Test
    void elContextoArrancaYElEsquemaEstaDisponible() {
        assertNotNull(context);
        assertTrue(context.isRunning());
        assertEquals(0L, productoRepository.count());
    }
}
