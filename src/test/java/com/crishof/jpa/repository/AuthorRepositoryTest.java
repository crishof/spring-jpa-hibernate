package com.crishof.jpa.repository;

import com.crishof.jpa.config.JpaAuditingConfig;
import com.crishof.jpa.dto.AuthorStatsDto;
import com.crishof.jpa.entity.Author;
import com.crishof.jpa.entity.AuthorProfile;
import com.crishof.jpa.entity.Post;
import com.crishof.jpa.enums.PostStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Tests del AuthorRepository: proyección DTO, JOIN FETCH del profile y
 * búsquedas derivadas. Se importa JpaAuditingConfig para la auditoría.
 */
@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("AuthorRepository — Tests @DataJpaTest con H2")
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Author savedAuthor;

    @BeforeEach
    void setUp() {
        Author author = new Author("Martin", "Fowler", "mfowler@test.com");
        // Asociar un profile bidireccional (cascade ALL guarda ambos)
        AuthorProfile profile = new AuthorProfile(author);
        profile.setGithubHandle("martinfowler");
        author.setProfile(profile);

        savedAuthor = authorRepository.save(author);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findByEmail() debe encontrar al autor por su email")
    void should_find_author_by_email() {
        Optional<Author> result = authorRepository.findByEmail("mfowler@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getLastName()).isEqualTo("Fowler");
    }

    @Test
    @DisplayName("existsByEmail() debe retornar true si el email existe")
    void should_return_true_when_email_exists() {
        assertThat(authorRepository.existsByEmail("mfowler@test.com")).isTrue();
        assertThat(authorRepository.existsByEmail("nadie@test.com")).isFalse();
    }

    @Test
    @DisplayName("findByIdWithProfile() debe cargar el profile con JOIN FETCH")
    void should_load_profile_with_join_fetch() {
        Optional<Author> result = authorRepository.findByIdWithProfile(savedAuthor.getId());

        assertThat(result).isPresent();
        // Acceder al profile fuera de sesión NO debe fallar (fue cargado con FETCH)
        assertThat(result.get().getProfile()).isNotNull();
        assertThat(result.get().getProfile().getGithubHandle()).isEqualTo("martinfowler");
    }

    @Test
    @DisplayName("findAuthorStats() debe retornar el DTO con conteo y media de vistas")
    void should_return_author_stats_dto() {
        // Arrange: crear dos posts publicados con vistas
        Author author = authorRepository.findById(savedAuthor.getId()).orElseThrow();
        Post p1 = new Post("Post A", "post-a", "contenido", author);
        p1.setStatus(PostStatus.PUBLISHED);
        p1.setViewCount(100L);
        Post p2 = new Post("Post B", "post-b", "contenido", author);
        p2.setStatus(PostStatus.PUBLISHED);
        p2.setViewCount(200L);
        author.addPost(p1);
        author.addPost(p2);
        authorRepository.save(author);
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<AuthorStatsDto> stats = authorRepository.findAuthorStats(savedAuthor.getId());

        // Assert
        assertThat(stats).isPresent();
        assertThat(stats.get().fullName()).isEqualTo("Martin Fowler");
        assertThat(stats.get().postCount()).isEqualTo(2);
        assertThat(stats.get().avgViews()).isEqualTo(150.0);
    }
}
