package com.crishof.jpa.entity;

import com.crishof.jpa.config.JpaAuditingConfig;
import com.crishof.jpa.enums.PostStatus;
import com.crishof.jpa.repository.AuthorRepository;
import com.crishof.jpa.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 * Demostración del bloqueo optimista (@Version) en un test @DataJpaTest.
 *
 * Escenario:
 * 1. Se persiste un Post (version = 0).
 * 2. Se obtiene una copia "obsoleta" (detached) con version = 0.
 * 3. Otra "transacción" carga el post fresco, lo actualiza y hace flush:
 *    la versión en BD pasa de 0 a 1.
 * 4. Al intentar persistir la copia obsoleta (aún version = 0), Hibernate
 *    detecta el conflicto → ObjectOptimisticLockingFailureException.
 */
@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("Optimistic Locking — @Version en acción")
class OptimisticLockTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Long postId;

    @BeforeEach
    void setUp() {
        Author author = authorRepository.save(new Author("Josh", "Long", "jlong@test.com"));
        Post post = new Post("Post original", "post-original", "contenido", author);
        post.setStatus(PostStatus.DRAFT);
        post = postRepository.save(post);
        postId = post.getId();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("La versión debe empezar en 0 y aumentar a 1 tras una actualización")
    void version_should_increment_on_update() {
        Post post = postRepository.findById(postId).orElseThrow();
        assertThat(post.getVersion()).isZero();

        post.setTitle("Título editado");
        postRepository.saveAndFlush(post);

        assertThat(post.getVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("Una actualización sobre una copia obsoleta debe lanzar ObjectOptimisticLockingFailureException")
    void should_throw_optimistic_lock_exception_on_stale_update() {
        // 1. Cargar el post y obtener una copia obsoleta (detached, version = 0)
        Post staleCopy = postRepository.findById(postId).orElseThrow();
        entityManager.detach(staleCopy);

        // 2. Otra "transacción" carga el post fresco y lo actualiza → version 0 → 1
        Post freshCopy = postRepository.findById(postId).orElseThrow();
        freshCopy.setTitle("Editado por Usuario A");
        postRepository.saveAndFlush(freshCopy);
        entityManager.clear();

        // 3. Intentar persistir la copia obsoleta (todavía version = 0) → conflicto
        staleCopy.setTitle("Editado por Usuario B");
        assertThatThrownBy(() -> postRepository.saveAndFlush(staleCopy))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
