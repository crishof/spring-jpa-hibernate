package com.crishof.jpa.entity;

import com.crishof.jpa.entity.base.BaseEntity;
import com.crishof.jpa.enums.PostStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Entidad central del proyecto.
 * Demuestra: @ManyToOne, @ManyToMany, @Version, @Enumerated.
 */
@Entity
@Table(name = "posts", indexes = {
        // Índice en slug para búsquedas frecuentes por URL
        @Index(name = "idx_post_slug", columnList = "slug"),
        // Índice compuesto para filtrar por autor y estado
        @Index(name = "idx_post_author_status", columnList = "author_id, status")
})
public class Post extends BaseEntity {

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, unique = true, length = 300)
    private String slug;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String summary;

    /*
     * @Enumerated(EnumType.STRING):
     * Almacena el nombre del enum como String ("DRAFT", "PUBLISHED", "ARCHIVED").
     *
     * NUNCA usar EnumType.ORDINAL (default):
     * - ORDINAL guarda 0, 1, 2... Si reordenas el enum, los datos se corrompen.
     * - STRING es seguro: el nombre del enum es estable aunque cambie el orden.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status = PostStatus.DRAFT;

    /*
     * @Version: habilita el bloqueo optimista (Optimistic Locking).
     *
     * Funcionamiento:
     * 1. Usuario A y Usuario B leen el Post con version = 1
     * 2. Usuario A actualiza → Hibernate hace:
     *    UPDATE posts SET ..., version = 2 WHERE id = ? AND version = 1
     *    → UPDATE exitoso, version pasa a 2
     * 3. Usuario B intenta actualizar → Hibernate hace:
     *    UPDATE posts SET ..., version = 2 WHERE id = ? AND version = 1
     *    → 0 filas afectadas (version ya es 2) → lanza OptimisticLockException
     *
     * El campo version lo gestiona HIBERNATE, nunca el código de aplicación.
     */
    @Version
    @Column(nullable = false)
    private Integer version = 0;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /*
     * @ManyToOne: relación con Author.
     * Post tiene la FK (author_id) → Post es el lado propietario.
     * FetchType.LAZY: NO cargar el Author automáticamente al cargar el Post.
     *
     * ¡IMPORTANTE! En Hibernate 7+, @ManyToOne sin fetch explícito
     * tiene EAGER por defecto según la spec JPA. Siempre ser explícito
     * con LAZY para evitar cargas innecesarias y claridad.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /*
     * @ManyToMany: relación con Tag.
     * Post es el lado PROPIETARIO (tiene @JoinTable).
     *
     * @JoinTable define la tabla de unión:
     * - name: nombre de la tabla de unión
     * - joinColumns: FK que apunta a la tabla de la entidad PROPIETARIA (Post)
     * - inverseJoinColumns: FK que apunta a la tabla de la entidad INVERSA (Tag)
     *
     * CascadeType: NO usar CascadeType.ALL en @ManyToMany.
     * Si eliminamos un Post no queremos eliminar los Tags (son compartidos).
     */
    // @JsonIgnore: colección LAZY; se carga para lógica interna (@EntityGraph)
    // pero no se serializa para evitar LazyInitializationException y ciclos.
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "post",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Comment> comments = new ArrayList<>();

    // Constructor vacío requerido por JPA
    protected Post() {
    }

    // Constructor con campos obligatorios
    public Post(String title, String slug, String content, Author author) {
        this.title = title;
        this.slug = slug;
        this.content = content;
        this.author = author;
    }

    // Métodos de conveniencia para gestionar la bidireccionalidad

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getPosts().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getPosts().remove(this);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setPost(null);
    }

    // Transición de estado con lógica de negocio
    public void publish() {
        if (this.status == PostStatus.ARCHIVED) {
            throw new IllegalStateException("No se puede publicar un post archivado");
        }
        this.status = PostStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    // Getters y setters (toString SIN colecciones ni relaciones lazy)

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public List<Comment> getComments() {
        return comments;
    }

    @Override
    public String toString() {
        return "Post{id=" + getId() + ", title='" + title + '\''
                + ", slug='" + slug + '\'' + ", status=" + status
                + ", version=" + version + '}';
    }
}
