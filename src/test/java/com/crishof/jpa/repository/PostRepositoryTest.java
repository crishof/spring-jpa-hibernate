package com.crishof.jpa.repository;

import com.crishof.jpa.config.JpaAuditingConfig;
import com.crishof.jpa.dto.PostSummaryDto;
import com.crishof.jpa.entity.Author;
import com.crishof.jpa.entity.Category;
import com.crishof.jpa.entity.Post;
import com.crishof.jpa.enums.PostStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * @DataJpaTest: contexto mínimo de Spring con solo la capa JPA.
 * - Configura un DataSource H2 en memoria
 * - Registra @Entity y @Repository beans
 * - Hace rollback después de cada test (@Transactional by default)
 * - NO carga @Service, @Controller, @RestController
 *
 * @Import(JpaAuditingConfig.class): @DataJpaTest no carga la clase principal,
 * así que importamos la config de auditoría para que @CreatedDate/@LastModifiedDate
 * se rellenen (columnas created_at/updated_at son NOT NULL).
 */
@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("PostRepository — Tests @DataJpaTest con H2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // TestEntityManager: alternativa a EntityManager en tests,
    // con métodos convenientes como persistAndFlush()
    @Autowired
    private TestEntityManager entityManager;

    private Author savedAuthor;
    private Category savedCategory;

    @BeforeEach
    void setUp() {
        // Crear datos base para los tests
        Author author = new Author("Josh", "Long", "jlong@test.com");
        savedAuthor = authorRepository.save(author);

        Category category = new Category("Spring", "Todo sobre Spring");
        savedCategory = categoryRepository.save(category);

        // Flush para que los datos estén en H2 antes de los tests
        entityManager.flush();
        entityManager.clear(); // limpiar contexto de persistencia (1er nivel de caché)
    }

    // Helper: crea un Post asociado al autor de prueba
    private Post createPost(String title, PostStatus status) {
        Author author = authorRepository.findById(savedAuthor.getId()).orElseThrow();
        Post post = new Post(title, slugify(title), "Contenido de " + title, author);
        post.setStatus(status);
        return post;
    }

    private String slugify(String title) {
        return title.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }

    @Nested
    @DisplayName("findByStatus()")
    class FindByStatusTests {

        @Test
        @DisplayName("Debe retornar solo posts publicados cuando status es PUBLISHED")
        void should_return_only_published_posts() {
            // Arrange: crear un post publicado y uno borrador
            Post published = createPost("Post publicado", PostStatus.PUBLISHED);
            Post draft = createPost("Post borrador", PostStatus.DRAFT);
            postRepository.saveAll(List.of(published, draft));
            entityManager.flush();
            entityManager.clear();

            // Act
            List<Post> result = postRepository.findByStatus(PostStatus.PUBLISHED);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Post publicado");
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay posts con ese estado")
        void should_return_empty_list_when_no_posts_with_status() {
            List<Post> result = postRepository.findByStatus(PostStatus.ARCHIVED);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findBySlugWithDetails() con @EntityGraph")
    class FindBySlugTests {

        @Test
        @DisplayName("Debe cargar author, category y tags en una sola query")
        void should_load_all_relations_with_entity_graph() {
            // Arrange
            Post post = createPost("Post con todo", PostStatus.PUBLISHED);
            post.setSlug("post-con-todo");
            Category category = categoryRepository.findById(savedCategory.getId()).orElseThrow();
            post.setCategory(category);
            postRepository.save(post);
            entityManager.flush();
            entityManager.clear();

            // Act: @EntityGraph hace JOIN FETCH de author, tags, category
            Optional<Post> result = postRepository.findBySlugWithDetails("post-con-todo");

            // Assert
            assertThat(result).isPresent();
            // Acceder a las relaciones NO debe lanzar LazyInitializationException
            // porque @EntityGraph las cargó eagerly
            assertThat(result.get().getAuthor().getFirstName()).isEqualTo("Josh");
            assertThat(result.get().getCategory().getName()).isEqualTo("Spring");
        }
    }

    @Nested
    @DisplayName("findAllSummaries() — interface-based projection")
    class ProjectionTests {

        @Test
        @DisplayName("Debe retornar DTOs con los campos correctos sin cargar entidades completas")
        void should_return_summary_dtos() {
            Post post = createPost("Post con proyección", PostStatus.PUBLISHED);
            postRepository.save(post);
            entityManager.flush();
            entityManager.clear();

            List<PostSummaryDto> summaries = postRepository.findAllSummaries();

            assertThat(summaries).isNotEmpty();
            // Verificar que el DTO tiene los campos correctos
            PostSummaryDto summary = summaries.get(0);
            assertThat(summary.getTitle()).isNotBlank();
            assertThat(summary.getAuthorFullName()).contains("Josh");
        }
    }

    @Nested
    @DisplayName("incrementViewCount() — @Modifying bulk update")
    class BulkUpdateTests {

        @Test
        @DisplayName("Debe incrementar viewCount sin cargar la entidad en memoria")
        void should_increment_view_count() {
            Post post = createPost("Post visto", PostStatus.PUBLISHED);
            post = postRepository.save(post);
            Long postId = post.getId();
            entityManager.flush();
            entityManager.clear();

            // Act: bulk update (no carga la entidad)
            int updated = postRepository.incrementViewCount(postId);
            entityManager.flush();
            entityManager.clear();

            // Assert: recargar el post para verificar el cambio
            Post reloaded = postRepository.findById(postId).orElseThrow();
            assertThat(updated).isEqualTo(1);
            assertThat(reloaded.getViewCount()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("countByAuthorId()")
    class CountTests {

        @Test
        @DisplayName("Debe contar los posts de un autor")
        void should_count_posts_by_author() {
            postRepository.saveAll(List.of(
                    createPost("Post uno", PostStatus.PUBLISHED),
                    createPost("Post dos", PostStatus.DRAFT)
            ));
            entityManager.flush();
            entityManager.clear();

            long count = postRepository.countByAuthorId(savedAuthor.getId());

            assertThat(count).isEqualTo(2);
        }
    }
}
