package com.crishof.jpa.service.demo;

import com.crishof.jpa.entity.Post;
import com.crishof.jpa.repository.PostRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/*
 * Demostración del PROBLEMA N+1 y sus SOLUCIONES.
 *
 * El problema N+1 es la causa más común de rendimiento deficiente con ORMs.
 * Ocurre cuando cargas N entidades y luego accedes a una relación LAZY
 * de cada una, generando N queries adicionales (1 principal + N secundarias).
 *
 * Este servicio demuestra el problema y tres soluciones:
 * 1. JOIN FETCH en JPQL
 * 2. @EntityGraph
 * 3. batch_fetch_size en Hibernate
 */
@Service
@Transactional(readOnly = true)
public class NPlusOneDemoService {

    private final PostRepository postRepository;
    private final EntityManager entityManager; // para queries directas con Criteria API

    public NPlusOneDemoService(PostRepository postRepository,
                               EntityManager entityManager) {
        this.postRepository = postRepository;
        this.entityManager = entityManager;
    }

    /*
     * ANTI-PATRÓN: genera N+1 queries.
     * Con 10 posts: 1 query para posts + 10 queries para autores = 11 queries
     * Con 100 posts: 101 queries. Con 1000: 1001 queries.
     *
     * Para detectarlo: enable hibernate.generate_statistics=true
     * y buscar en los logs "HHH000117: HQL: ... N queries executed"
     */
    public List<String> demonstrateNPlusOneProblem() {
        // Esta query solo carga posts (author no se carga aún)
        List<Post> posts = postRepository.findAll();

        // AQUÍ ocurre N+1: cada acceso a post.getAuthor() dispara una query
        return posts.stream()
                .map(p -> p.getTitle() + " by " + p.getAuthor().getFirstName())
                // ↑ getAuthor() = query adicional por cada post → PROBLEMA N+1
                .toList();
    }

    /*
     * SOLUCIÓN 1: JOIN FETCH en JPQL.
     * Una sola query con JOIN carga posts Y autores juntos.
     * El SQL generado: SELECT p.*, a.* FROM posts p JOIN authors a ON p.author_id = a.id
     */
    public List<String> solveWithJoinFetch() {
        // findPublishedPostsWithAuthor() usa JOIN FETCH → 1 sola query
        List<Post> posts = postRepository.findPublishedPostsWithAuthor();

        // Ahora getAuthor() no genera queries adicionales (ya está cargado)
        return posts.stream()
                .map(p -> p.getTitle() + " by " + p.getAuthor().getFirstName())
                .toList();
    }

    /*
     * SOLUCIÓN 2: @EntityGraph.
     * Equivalente a JOIN FETCH pero declarativo en la anotación del método.
     * Spring JPA añade el JOIN automáticamente a la query generada.
     */
    public Optional<Post> solveWithEntityGraph(String slug) {
        // findBySlugWithDetails usa @EntityGraph → carga author, tags y category
        return postRepository.findBySlugWithDetails(slug);
    }

    /*
     * SOLUCIÓN 3: default_batch_fetch_size (configurado en application.yml).
     * Hibernate agrupa las queries de lazy loading en lotes.
     * En vez de N queries de 1 elemento, hace N/20 queries de 20 elementos.
     * SELECT * FROM authors WHERE id IN (1,2,3,...,20)
     * No requiere cambios en el código: es configuración de Hibernate.
     */
    public String explainBatchFetchSize() {
        return """
                batch_fetch_size=20 configurado en application.yml:
                En vez de N queries individuales para lazy loading,
                Hibernate agrupa las cargas:
                SELECT * FROM authors WHERE id IN (?, ?, ..., ?)  [hasta 20 ids]
                Reduce N queries a N/20 queries.
                """;
    }
}
