package com.app;

import com.app.exception.BusinessException;
import com.app.producto.dto.ProductoDto;
import com.app.producto.dto.ProductoFormDto;
import com.app.producto.model.Producto;
import com.app.producto.repository.ProductoRepository;
import com.app.producto.service.ProductoService;
import com.app.producto_categoria.model.ProductoCategoria;
import com.app.producto_categoria.repository.ProductoCategoriaRepository;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(propertySources = "classpath:application-test.properties")
class IntegridadTest {

    @Inject
    ProductoService productoService;

    @Inject
    ProductoRepository productoRepository;

    @Inject
    ProductoCategoriaRepository productoCategoriaRepository;

    private ProductoFormDto form(String nombre, String sku, String categoria) {
        ProductoCategoria cat = productoCategoriaRepository.save(new ProductoCategoria(categoria, "Prueba"));
        ProductoFormDto dto = new ProductoFormDto(nombre, sku, new BigDecimal("100"), 5, categoria);
        dto.setCategoriaId(cat.getId());
        return dto;
    }

    @Test
    void skuDuplicadoEsRechazadoPorLaBase() {
        productoService.save(form("Uno", "SKU-DUP", "Test"));
        ProductoFormDto duplicado = new ProductoFormDto("Dos", "SKU-DUP", new BigDecimal("200"), 3, "Test");
        duplicado.setCategoriaId(productoCategoriaRepository.findAll().iterator().next().getId());

        assertThrows(DataAccessException.class, () -> productoService.save(duplicado));
    }

    @Test
    void borrarDesactivaSinEliminarYCompletaTimestamps() {
        ProductoDto creado = productoService.save(form("Producto soft", "SKU-SOFT", "Test"));

        Producto enBase = productoRepository.findById(creado.getId()).orElseThrow();
        assertNotNull(enBase.getCreadoEn());
        assertNotNull(enBase.getCategoriaId());

        assertTrue(productoService.delete(creado.getId()));

        Producto despues = productoRepository.findById(creado.getId()).orElseThrow();
        assertFalse(despues.isActivo());
        assertFalse(productoService.findAll().stream().anyMatch(p -> p.getId().equals(creado.getId())));
    }

    @Test
    void categoriaInexistenteEsRechazada() {
        ProductoFormDto dto = new ProductoFormDto("Sin categoria", "SKU-SIN-CAT", new BigDecimal("10"), 1, "NoExiste");

        assertThrows(BusinessException.class, () -> productoService.save(dto));
    }
}
