package com.crishof.jpa.dto;

/*
 * Class-based projection (record): Spring JPA llama al constructor
 * con los valores en el mismo orden que en la cláusula SELECT de la query.
 *
 * El constructor del record debe coincidir exactamente con los tipos
 * retornados por la query JPQL (ver AuthorRepository.findAuthorStats).
 */
public record AuthorStatsDto(
        String fullName,
        long postCount,
        double avgViews
) {
}
