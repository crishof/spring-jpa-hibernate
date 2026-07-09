package com.crishof.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/*
 * Tag: lado inverso de la relación @ManyToMany con Post.
 * La tabla de unión post_tags tiene las FKs.
 * El lado propietario se define en Post.
 */
@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    /*
     * Lado INVERSO de la relación @ManyToMany.
     * mappedBy = "tags": Post tiene el @JoinTable.
     * No se especifica @JoinTable aquí: Hibernate lo toma del lado propietario.
     */
    @JsonIgnore
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();

    // Constructor vacío requerido por JPA
    protected Tag() {
    }

    public Tag(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    // Getters y setters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public List<Post> getPosts() {
        return posts;
    }
}
