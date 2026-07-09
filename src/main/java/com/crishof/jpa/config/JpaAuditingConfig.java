package com.crishof.jpa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Habilita la auditoría de Spring Data JPA.
 *
 * @EnableJpaAuditing activa el {@code AuditingEntityListener} para que
 * @CreatedDate y @LastModifiedDate de {@link com.crishof.jpa.entity.base.BaseEntity}
 * se rellenen automáticamente al persistir y actualizar entidades.
 *
 * Se coloca en una clase @Configuration independiente (en lugar de sobre la
 * clase principal) para poder importarla en los tests @DataJpaTest con
 * @Import(JpaAuditingConfig.class): @DataJpaTest no carga la aplicación completa,
 * por lo que sin este import la auditoría no se activaría y las columnas
 * created_at/updated_at (NOT NULL) quedarían nulas.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
