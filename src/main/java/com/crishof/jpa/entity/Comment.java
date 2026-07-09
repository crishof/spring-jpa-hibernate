package com.crishof.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Self-referencing @ManyToOne para comentarios anidados (respuestas).
 * Un comentario puede tener un padre (es una respuesta) o no (es raíz).
 */
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_name", nullable = false, length = 100)
    private String authorName;

    @Column(name = "author_email", length = 255)
    private String authorEmail;

    @Column(nullable = false)
    private Boolean approved = false;

    // @JsonIgnore en las relaciones LAZY para evitar ciclos y lazy loading.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /*
     * Auto-referencia: un comentario puede ser respuesta a otro.
     * parent == null → comentario raíz (no es respuesta)
     * parent != null → respuesta a un comentario padre
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @JsonIgnore
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Comment> replies = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructor vacío requerido por JPA
    protected Comment() {
    }

    public Comment(String content, String authorName, Post post) {
        this.content = content;
        this.authorName = authorName;
        this.post = post;
    }

    // Método de conveniencia para respuestas anidadas
    public void addReply(Comment reply) {
        replies.add(reply);
        reply.setParent(this);
        reply.setPost(this.post);
    }

    // @JsonIgnore: método derivado que accede a la relación LAZY 'parent'.
    @JsonIgnore
    public boolean isReply() {
        return parent != null;
    }

    // Getters y setters

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Comment getParent() {
        return parent;
    }

    public void setParent(Comment parent) {
        this.parent = parent;
    }

    public List<Comment> getReplies() {
        return replies;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
