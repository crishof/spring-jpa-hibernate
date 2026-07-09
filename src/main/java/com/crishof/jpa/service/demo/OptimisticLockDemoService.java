package com.crishof.jpa.service.demo;

import com.crishof.jpa.entity.Post;
import com.crishof.jpa.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * Demostración de Optimistic Locking con @Version.
 *
 * Escenario simulado:
 * - Dos "usuarios" leen el mismo Post (misma versión)
 * - Usuario A actualiza el título (version 1 → 2)
 * - Usuario B intenta actualizar el resumen (version 1 → conflicto)
 * - JPA lanza OptimisticLockException en la transacción de B
 *
 * Esto evita la "lost update" sin bloquear la fila para lectura.
 */
@Service
public class OptimisticLockDemoService {

    private final PostRepository postRepository;

    public OptimisticLockDemoService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /*
     * Simula una actualización con incremento de versión.
     * En código real los dos updates ocurrirían en hilos/transacciones distintos.
     * El test OptimisticLockTest demuestra el conflicto real de versiones.
     */
    @Transactional
    public String demonstrateOptimisticLock(Long postId) {
        // Cargar el post: tiene version = N
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post no encontrado: " + postId));

        int currentVersion = post.getVersion();

        // Primera actualización: éxito → version pasa de N a N+1
        post.setTitle(post.getTitle() + " [Editado por Usuario A]");
        postRepository.saveAndFlush(post);

        return String.format(
                "Actualización de Usuario A exitosa. Version: %d → %d",
                currentVersion, post.getVersion()
        );
    }

    // En los tests se demuestra que la segunda actualización concurrente
    // lanza OptimisticLockException o ObjectOptimisticLockingFailureException
}
