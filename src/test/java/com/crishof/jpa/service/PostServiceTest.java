package com.crishof.jpa.service;

import com.crishof.jpa.entity.Author;
import com.crishof.jpa.entity.Post;
import com.crishof.jpa.enums.PostStatus;
import com.crishof.jpa.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Tests unitarios del PostService con Mockito puro (sin contexto Spring).
 *
 * @ExtendWith(MockitoExtension.class): activa la creación de mocks.
 * @Mock: crea un doble del repositorio. @InjectMocks: inyecta los mocks
 * en el servicio bajo prueba. No hay base de datos: se verifica la lógica
 * de negocio y las interacciones con el repositorio.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PostService — Tests unitarios con Mockito")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    private Post samplePost() {
        Author author = new Author("Josh", "Long", "jlong@test.com");
        Post post = new Post("Título", "titulo", "contenido", author);
        post.setStatus(PostStatus.DRAFT);
        return post;
    }

    @Test
    @DisplayName("publish() debe cambiar el estado a PUBLISHED cuando el post existe")
    void should_publish_post() {
        Post post = samplePost();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.publish(1L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.getPublishedAt()).isNotNull();
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("publish() debe lanzar EntityNotFoundException cuando el post no existe")
    void should_throw_when_post_not_found_on_publish() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.publish(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("registerView() debe llamar al bulk update incrementViewCount")
    void should_register_view() {
        when(postRepository.incrementViewCount(1L)).thenReturn(1);

        postService.registerView(1L);

        verify(postRepository, times(1)).incrementViewCount(1L);
    }

    @Test
    @DisplayName("registerView() debe lanzar EntityNotFoundException cuando no se actualiza ninguna fila")
    void should_throw_when_no_rows_updated_on_view() {
        when(postRepository.incrementViewCount(anyLong())).thenReturn(0);

        assertThatThrownBy(() -> postService.registerView(42L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("findByIdWithDetails() debe lanzar EntityNotFoundException cuando no existe")
    void should_throw_when_details_not_found() {
        when(postRepository.findByIdWithAuthorAndCategory(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.findByIdWithDetails(5L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
