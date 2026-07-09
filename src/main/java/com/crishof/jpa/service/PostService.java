package com.crishof.jpa.service;

import com.crishof.jpa.dto.PostSummaryDto;
import com.crishof.jpa.entity.Post;
import com.crishof.jpa.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/*
 * Servicio de posts: orquesta la lógica de negocio sobre la entidad central.
 *
 * @Transactional(readOnly = true) a nivel de clase: por defecto todas las
 * operaciones son de solo lectura (optimización: Hibernate no hace dirty checking).
 * Los métodos que escriben se anotan con @Transactional (readOnly = false).
 */
@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // Lista resumida usando interface-based projection (sin cargar entidades completas)
    public List<PostSummaryDto> findAllSummaries() {
        return postRepository.findAllSummaries();
    }

    // Detalle completo con JOIN FETCH de author y category (evita N+1)
    public Post findByIdWithDetails(Long id) {
        return postRepository.findByIdWithAuthorAndCategory(id)
                .orElseThrow(() -> new EntityNotFoundException("Post no encontrado: " + id));
    }

    // Búsqueda por slug con @EntityGraph (carga author, tags y category)
    public Optional<Post> findBySlug(String slug) {
        return postRepository.findBySlugWithDetails(slug);
    }

    /*
     * Publicar un post: transición de estado con lógica de negocio.
     * @Transactional (escritura): el dirty checking de Hibernate detecta
     * el cambio de estado y genera el UPDATE al hacer commit.
     */
    @Transactional
    public void publish(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post no encontrado: " + id));
        post.publish(); // valida la transición y fija publishedAt
        // No hace falta save(): la entidad está gestionada y se persiste en el commit
    }

    /*
     * Incrementar el contador de vistas con bulk update (@Modifying).
     * No carga la entidad en memoria: ejecuta un UPDATE directo.
     */
    @Transactional
    public void registerView(Long id) {
        int updated = postRepository.incrementViewCount(id);
        if (updated == 0) {
            throw new EntityNotFoundException("Post no encontrado: " + id);
        }
    }
}
