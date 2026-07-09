package com.crishof.jpa.repository;

import com.crishof.jpa.dto.AuthorStatsDto;
import com.crishof.jpa.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/*
 * Repositorio de autores con proyecciones DTO, JOIN FETCH y subqueries JPQL.
 */
public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findByEmail(String email);

    boolean existsByEmail(String email);

    /*
     * Class-based projection con DTO:
     * En lugar de interface-based (proxy), usa un record o clase Java real.
     * Spring JPA llama al constructor con los valores de la query.
     *
     * AuthorStatsDto es un record: record AuthorStatsDto(String fullName, long postCount, double avgViews)
     * La query usa "new" JPQL syntax con el nombre completo de la clase.
     */
    @Query("""
            SELECT new com.crishof.jpa.dto.AuthorStatsDto(
                CONCAT(a.firstName, ' ', a.lastName),
                COUNT(p.id),
                COALESCE(AVG(p.viewCount), 0.0)
            )
            FROM Author a LEFT JOIN a.posts p
            WHERE a.id = :authorId
            GROUP BY a.id, a.firstName, a.lastName
            """)
    Optional<AuthorStatsDto> findAuthorStats(@Param("authorId") Long authorId);

    /*
     * JPQL con JOIN FETCH del profile (@OneToOne LAZY):
     * Sin esto, acceder a author.getProfile() fuera de una transacción
     * lanza LazyInitializationException.
     */
    @Query("SELECT a FROM Author a JOIN FETCH a.profile WHERE a.id = :id")
    Optional<Author> findByIdWithProfile(@Param("id") Long id);

    // Autores con al menos N posts publicados (subquery correlacionada)
    @Query("""
            SELECT a FROM Author a
            WHERE (SELECT COUNT(p) FROM Post p
                   WHERE p.author = a AND p.status = 'PUBLISHED') >= :minPosts
            """)
    List<Author> findAuthorsWithMinPublishedPosts(@Param("minPosts") long minPosts);
}
