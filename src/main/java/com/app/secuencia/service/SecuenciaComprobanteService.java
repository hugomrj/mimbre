package com.app.secuencia.service;

import com.app.exception.BusinessException;
import com.app.secuencia.model.SecuenciaComprobante;
import com.app.secuencia.repository.SecuenciaComprobanteRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

/**
 * Genera el siguiente numero de comprobante de forma atomica. La fila se bloquea
 * con FOR UPDATE y el incremento ocurre en la misma transaccion del documento.
 */
@Singleton
public class SecuenciaComprobanteService {

    private final SecuenciaComprobanteRepository secuenciaRepository;

    public SecuenciaComprobanteService(SecuenciaComprobanteRepository secuenciaRepository) {
        this.secuenciaRepository = secuenciaRepository;
    }

    @Transactional
    public synchronized String siguiente(String tipo) {
        SecuenciaComprobante secuencia = secuenciaRepository.findByTipoForUpdate(tipo)
                .orElseThrow(() -> new BusinessException("No hay una secuencia configurada para " + tipo + "."));

        long numero = (secuencia.getUltimoNumero() != null ? secuencia.getUltimoNumero() : 0L) + 1L;
        secuencia.setUltimoNumero(numero);
        secuenciaRepository.update(secuencia);

        int longitud = secuencia.getLongitud() != null ? secuencia.getLongitud() : 7;
        return secuencia.getPrefijo() + String.format("%0" + longitud + "d", numero);
    }
}
