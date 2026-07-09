package com.crishof.jpa.dto;

import com.crishof.jpa.enums.PostStatus;

import java.time.LocalDateTime;

/*
 * Parámetros de búsqueda para Criteria API.
 * Todos los campos son opcionales (null = no filtrar).
 */
public record PostSearchCriteria(
        String title,
        PostStatus status,
        Long categoryId,
        LocalDateTime publishedAfter
) {
}
