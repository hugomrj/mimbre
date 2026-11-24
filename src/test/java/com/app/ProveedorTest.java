package com.app;

import com.app.exception.BusinessException;
import com.app.proveedor.ProveedorDto;
import com.app.proveedor.ProveedorService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(propertySources = "classpath:application-test.properties")
class ProveedorTest {

    @Inject
    Validator validator;

    @Inject
    ProveedorService proveedorService;

    @Test
    void emailVacioEsValidoYEmailInvalidoNo() {
        ProveedorDto vacio = new ProveedorDto();
        vacio.setEmail("");
        assertTrue(validator.validate(vacio).isEmpty());

        ProveedorDto invalido = new ProveedorDto();
        invalido.setEmail("abc");
        assertFalse(validator.validate(invalido).isEmpty());
    }

    @Test
    void rucVacioEsRechazado() {
        ProveedorDto dto = new ProveedorDto();
        dto.setNombre("Proveedor sin RUC");
        dto.setRuc("");
        dto.setEmail("");

        assertThrows(BusinessException.class, () -> proveedorService.save(dto));
    }

    @Test
    void guardaProveedorConRucSinEmail() {
        ProveedorDto dto = new ProveedorDto();
        dto.setNombre("Proveedor con RUC");
        dto.setRuc("80000000-1");
        dto.setEmail("");

        ProveedorDto guardado = proveedorService.save(dto);

        assertNotNull(guardado.getId());
        assertTrue(proveedorService.findById(guardado.getId()).isPresent());
    }
}
