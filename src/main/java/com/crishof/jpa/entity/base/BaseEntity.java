package com.crishof.jpa.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/*
 * @MappedSuperclass: clase base para todas las entidades.
 *
 * A diferencia de @Entity, @MappedSuperclass NO crea tabla propia.
 * Sus columnas se incluyen en la tabla de cada clase hija.
 * Ideal para campos comunes: id, createdAt, updatedAt.
 *
 * @EntityListeners(AuditingEntityListener.class) + @EnableJpaAuditing
 * en la clase de configuración permiten que Spring rellene automáticamente
 * @CreatedDate y @LastModifiedDate.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /*
     * GenerationType.IDENTITY: usa la secuencia SERIAL/BIGSERIAL de la BD.
     * En PostgreSQL es equivalente a SEQUENCE pero gestionado por la BD.
     *
     * Alternativa preferida en Hibernate 7: GenerationType.SEQUENCE con
     * @SequenceGenerator, porque permite batch inserts (IDENTITY los impide).
     * Aquí usamos IDENTITY por simplicidad; en producción preferir SEQUENCE.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @CreatedDate: Spring JPA Auditing rellena esto automáticamente al persistir
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // @LastModifiedDate: Spring actualiza esto en cada merge/save
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Getters (sin setters para id y fechas: solo el framework los escribe)

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
