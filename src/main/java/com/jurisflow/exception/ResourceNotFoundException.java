package com.jurisflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção para recursos não encontrados.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s não encontrado com %s: '%s'", resource, field, value));
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(String.format("%s não encontrado com id: '%s'", resource, id));
    }
}


