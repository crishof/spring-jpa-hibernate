package com.crishof.jpa.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/*
 * Manejo centralizado de excepciones JPA/Hibernate.
 *
 * CORRECCIÓN Spring Boot 4: en los tests usar @MockitoBean (no @MockBean,
 * que fue eliminado en Boot 4).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /*
     * OptimisticLockException: conflicto de versión en @Version.
     * Spring lo envuelve en ObjectOptimisticLockingFailureException.
     * Retornar 409 Conflict es la convención REST para este caso.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex) {
        return Map.of(
                "status", 409,
                "error", "Conflict",
                "message", "El recurso fue modificado por otra operación. Reintenta con los datos actualizados.",
                "timestamp", Instant.now()
        );
    }

    /*
     * DataIntegrityViolationException: violación de constraints de BD.
     * Ej: insertar email duplicado (UNIQUE constraint).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleDataIntegrity(
            DataIntegrityViolationException ex) {
        return Map.of(
                "status", 409,
                "error", "Conflict",
                "message", "Violación de restricción de integridad. Verifica duplicados.",
                "timestamp", Instant.now()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleEntityNotFound(
            EntityNotFoundException ex, HttpServletRequest req) {
        return Map.of(
                "status", 404,
                "error", "Not Found",
                "message", ex.getMessage(),
                "path", req.getRequestURI()
        );
    }

    /*
     * Argumentos inválidos de negocio (ej: email duplicado detectado en el service).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException ex) {
        return Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", ex.getMessage(),
                "timestamp", Instant.now()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneric(Exception ex) {
        // Registrar el error real en los logs para diagnóstico (no se expone al cliente)
        log.error("Error interno no controlado", ex);
        return Map.of(
                "status", 500,
                "error", "Internal Server Error",
                "message", "Error interno. Ver logs del servidor."
        );
    }
}
