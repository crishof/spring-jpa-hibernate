package com.crishof.jpa.dto;

import com.crishof.jpa.enums.PostStatus;

import java.time.LocalDateTime;

/*
 * Interface-based projection: Spring JPA crea un proxy que implementa
 * esta interface, mapeando cada método a la columna del mismo nombre.
 *
 * El nombre del método corresponde a los alias de la query JPQL:
 * "p.id as id" → getId()
 * "a.firstName as authorFirstName" → getAuthorFirstName()
 *
 * Ventaja: solo se cargan los campos necesarios, no la entidad completa.
 * Uso: listas, búsquedas, respuestas ligeras donde no se necesita todo.
 */
public interface PostSummaryDto {

    Long getId();

    String getTitle();

    String getSlug();

    String getSummary();

    PostStatus getStatus();

    LocalDateTime getPublishedAt();

    String getAuthorFirstName();

    String getAuthorLastName();

    // Método default: lógica en el DTO sin tocar el repositorio
    default String getAuthorFullName() {
        return getAuthorFirstName() + " " + getAuthorLastName();
    }
}
