package com.app.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Produces
@Singleton
@Requires(classes = {Throwable.class, ExceptionHandler.class})
public class GlobalExceptionHandler implements ExceptionHandler<Throwable, HttpResponse<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public HttpResponse<?> handle(HttpRequest request, Throwable exception) {
        LOG.error("Excepción capturada en GlobalExceptionHandler [URI: {}]: {}", 
                request.getUri(), exception.getMessage(), exception);

        boolean isUiRequest = request.getPath().startsWith("/ui") || request.getHeaders().contains("HX-Request");

        HttpStatus status = determineHttpStatus(exception);
        String errorMessage = formatMessage(exception);

        if (isUiRequest) {
            Map<String, Object> errorPayload = new LinkedHashMap<>();
            errorPayload.put("error", errorMessage);
            errorPayload.put("path", request.getPath());

            return HttpResponse.ok(errorPayload)
                    .status(status)
                    .header("HX-Trigger", "{\"showToast\": {\"message\": \"" + sanitize(errorMessage) + "\", \"type\": \"error\"}}");
        } else {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now().toString());
            body.put("status", status.getCode());
            body.put("error", status.getReason());
            body.put("message", errorMessage);
            body.put("path", request.getPath());

            return HttpResponse.status(status).body(body);
        }
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof ResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof StockInsuficienteException || ex instanceof BusinessException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ex instanceof ConstraintViolationException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ex instanceof IllegalArgumentException || ex instanceof IllegalStateException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String formatMessage(Throwable ex) {
        if (ex instanceof ConstraintViolationException cve) {
            return cve.getConstraintViolations().stream()
                    .map(v -> v.getMessage())
                    .collect(Collectors.joining(". "));
        }
        return ex.getMessage() != null ? ex.getMessage() : "Ha ocurrido un error inesperado en el servidor.";
    }

    private String sanitize(String input) {
        if (input == null) return "";
        return input.replace("\"", "'").replace("\n", " ");
    }
}
