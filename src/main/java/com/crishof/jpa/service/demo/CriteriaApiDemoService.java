package com.crishof.jpa.service.demo;

import com.crishof.jpa.dto.PostSearchCriteria;
import com.crishof.jpa.entity.Category;
import com.crishof.jpa.entity.Post;
import com.crishof.jpa.enums.PostStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * Criteria API: construcción de queries JPA de forma programática.
 *
 * Ventajas sobre JPQL:
 * - Type-safe: los errores de nombre de campo se detectan en compilación
 * - Dinámica: construir queries con condiciones opcionales
 *
 * Desventajas:
 * - Verbose: más código que JPQL para la misma query
 * - Menos legible para queries complejas
 *
 * Cuándo usar Criteria API vs JPQL:
 * - Queries con múltiples filtros OPCIONALES → Criteria API
 * - Queries fijas y claras → JPQL (más legible)
 * - En Spring Data JPA: Specifications (abstracción sobre Criteria API)
 *   → se verá en profundidad en el proyecto 06
 */
@Service
@Transactional(readOnly = true)
public class CriteriaApiDemoService {

    private final EntityManager entityManager;

    public CriteriaApiDemoService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /*
     * Búsqueda dinámica: construir predicados solo para los filtros presentes.
     * Si title==null, no se añade el predicado de título.
     */
    public List<Post> searchPosts(PostSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Post> query = cb.createQuery(Post.class);
        Root<Post> post = query.from(Post.class);

        // Fetch joins de las relaciones @ManyToOne (author y category):
        // dejan las entidades devueltas inicializadas para poder serializarlas
        // fuera de sesión (open-in-view=false) y evitan el N+1 al recorrerlas.
        post.fetch("author", JoinType.LEFT);
        Fetch<Post, Category> categoryFetch = post.fetch("category", JoinType.LEFT);

        // Construir lista de predicados dinámicamente
        List<Predicate> predicates = new ArrayList<>();

        // Filtro por título (búsqueda parcial, case-insensitive)
        if (criteria.title() != null && !criteria.title().isBlank()) {
            predicates.add(cb.like(
                    cb.lower(post.get("title")),
                    "%" + criteria.title().toLowerCase() + "%"
            ));
        }

        // Filtro por estado
        if (criteria.status() != null) {
            predicates.add(cb.equal(post.get("status"), criteria.status()));
        }

        // Filtro por rango de fecha de publicación
        if (criteria.publishedAfter() != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    post.get("publishedAt"), criteria.publishedAfter()
            ));
        }

        // Filtro por categoría: reutilizamos el fetch join anterior casteándolo
        // a Join (en Hibernate un Fetch es también un Join) para no duplicar joins.
        if (criteria.categoryId() != null) {
            @SuppressWarnings("unchecked")
            Join<Post, Category> category = (Join<Post, Category>) categoryFetch;
            predicates.add(cb.equal(category.get("id"), criteria.categoryId()));
        }

        // Aplicar todos los predicados con AND
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        // Ordenar por fecha de publicación descendente
        query.orderBy(cb.desc(post.get("publishedAt")));

        return entityManager.createQuery(query).getResultList();
    }

    /*
     * Ejemplo de Criteria API con agregación:
     * Contar posts agrupados por estado.
     * Retorna Map<PostStatus, Long>
     */
    public Map<PostStatus, Long> countPostsByStatus() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Post> post = query.from(Post.class);

        // SELECT p.status, COUNT(p) FROM posts p GROUP BY p.status
        query.multiselect(post.get("status"), cb.count(post))
                .groupBy(post.get("status"));

        List<Object[]> results = entityManager.createQuery(query).getResultList();

        return results.stream()
                .collect(Collectors.toMap(
                        row -> (PostStatus) row[0],
                        row -> (Long) row[1]
                ));
    }
}
