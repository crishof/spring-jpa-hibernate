package com.crishof.jpa.repository;

import com.crishof.jpa.dto.PostSummaryDto;
import com.crishof.jpa.entity.Post;
import com.crishof.jpa.enums.PostStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/*
 * Repositorio de Posts con ejemplos de todos los tipos de queries.
 * Extiende JpaRepository para tener save/findById/findAll/delete gratis.
 *
 * Nota: en spring-data-jpa (proyecto 06) se profundizará en
 * Specifications, Projections y queries derivadas del nombre.
 * Aquí el foco es JPQL y Criteria API de JPA puro.
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    /*
     * JPQL básica: HQL de Hibernate modernizado.
     * Diferencia con SQL nativo: trabaja con entidades y sus campos Java,
     * no con tablas y columnas de la BD.
     * "p.author.firstName" traversa la relación sin necesidad de JOIN explícito
     * (Hibernate genera el JOIN automáticamente).
     */
    @Query("SELECT p FROM Post p WHERE p.status = :status ORDER BY p.createdAt DESC")
    List<Post> findByStatus(@Param("status") PostStatus status);

    /*
     * JOIN FETCH: solución al problema N+1.
     * Sin JOIN FETCH:
     *   SELECT * FROM posts               → 1 query
     *   SELECT * FROM authors WHERE id=1  → 1 query por post (N queries)
     *   Total: N+1 queries
     *
     * Con JOIN FETCH:
     *   SELECT p, a FROM posts p JOIN FETCH p.author a → 1 sola query
     *   Total: 1 query
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.status = 'PUBLISHED'")
    List<Post> findPublishedPostsWithAuthor();

    /*
     * JPQL con múltiples JOIN FETCH:
     * Cargar post + author + category en una sola query.
     * Cuidado: NO hacer JOIN FETCH de dos colecciones (@OneToMany) en la
     * misma query → produce resultado cartesiano y MultipleBagFetchException.
     */
    @Query("""
            SELECT DISTINCT p FROM Post p
            JOIN FETCH p.author a
            LEFT JOIN FETCH p.category c
            WHERE p.id = :id
            """)
    Optional<Post> findByIdWithAuthorAndCategory(@Param("id") Long id);

    /*
     * @EntityGraph: alternativa a JOIN FETCH en la anotación.
     * Más declarativo y reutilizable que repetir JOIN FETCH en cada query.
     * attributePaths: lista de relaciones a cargar eagerly para ESTA query.
     */
    @EntityGraph(attributePaths = {"author", "tags", "category"})
    @Query("SELECT p FROM Post p WHERE p.slug = :slug")
    Optional<Post> findBySlugWithDetails(@Param("slug") String slug);

    /*
     * Proyección JPQL: seleccionar solo columnas necesarias.
     * Más eficiente que cargar la entidad completa cuando solo
     * se necesitan unos pocos campos.
     *
     * PostSummaryDto es una interface-based projection:
     * Spring JPA crea un proxy que implementa la interface con los valores.
     */
    @Query("""
            SELECT p.id as id, p.title as title, p.slug as slug,
                   p.summary as summary, p.status as status,
                   p.publishedAt as publishedAt,
                   a.firstName as authorFirstName, a.lastName as authorLastName
            FROM Post p JOIN p.author a
            ORDER BY p.createdAt DESC
            """)
    List<PostSummaryDto> findAllSummaries();

    /*
     * Contador JPQL: query de agregación.
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.author.id = :authorId")
    long countByAuthorId(@Param("authorId") Long authorId);

    /*
     * Native query: SQL puro cuando JPQL no es suficiente.
     * Caso de uso: funciones específicas de PostgreSQL, CTEs, full-text search.
     * nativeQuery = true: Hibernate pasa el SQL directamente a la BD.
     *
     * DESVENTAJA: pierde portabilidad entre bases de datos.
     */
    @Query(value = """
            SELECT p.* FROM posts p
            WHERE to_tsvector('spanish', p.title || ' ' || p.content)
                  @@ plainto_tsquery('spanish', :searchTerm)
            ORDER BY p.published_at DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Post> fullTextSearch(@Param("searchTerm") String searchTerm,
                              @Param("limit") int limit);

    /*
     * Bulk update con @Modifying:
     * Actualizar múltiples registros en una sola query (sin cargarlos en memoria).
     * @Modifying es obligatorio para INSERT/UPDATE/DELETE en JPQL.
     * clearAutomatically = true: limpia el contexto de persistencia para
     * evitar que Hibernate tenga datos obsoletos en caché de primer nivel.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    int incrementViewCount(@Param("id") Long id);

    /*
     * Derived query method (Spring Data): método que Spring implementa
     * automáticamente derivando la query del nombre del método.
     * En el proyecto 06 se explorarán en profundidad.
     */
    List<Post> findByAuthorIdAndStatus(Long authorId, PostStatus status);

    // Contar posts por categoría
    long countByCategoryId(Long categoryId);
}
