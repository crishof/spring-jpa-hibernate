package com.crishof.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación.
 *
 * El escaneo de componentes de @SpringBootApplication registra automáticamente
 * la clase {@link com.crishof.jpa.config.JpaAuditingConfig}, que habilita
 * la auditoría JPA (@CreatedDate / @LastModifiedDate) mediante @EnableJpaAuditing.
 * Se aísla en una @Configuration aparte para poder importarla selectivamente
 * en los tests @DataJpaTest (que no cargan la clase principal).
 */
@SpringBootApplication
public class JpaHibernateApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaHibernateApplication.class, args);
    }

}
