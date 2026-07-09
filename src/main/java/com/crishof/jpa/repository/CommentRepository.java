package com.crishof.jpa.repository;

import com.crishof.jpa.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/*
 * Repositorio de comentarios. Incluye consulta de comentarios raíz
 * (parent IS NULL) de un post: los que no son respuestas anidadas.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Comentarios raíz de un post (sin padre): el primer nivel de la conversación
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL")
    List<Comment> findRootCommentsByPostId(@Param("postId") Long postId);

    List<Comment> findByPostIdAndApprovedTrue(Long postId);
}
