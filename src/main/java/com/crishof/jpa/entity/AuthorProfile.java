package com.crishof.jpa.entity;

import com.crishof.jpa.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/*
 * @OneToOne: AuthorProfile tiene la FK (author_id) → es el lado propietario.
 * El lado inverso (mappedBy = "author") está en Author.
 *
 * @JoinColumn(name = "author_id", unique = true): la columna author_id
 * en author_profiles es única → garantiza la cardinalidad OneToOne.
 */
@Entity
@Table(name = "author_profiles")
public class AuthorProfile extends BaseEntity {

    /*
     * AuthorProfile es el lado propietario porque tiene @JoinColumn.
     * En una relación @OneToOne bidireccional, el lado con @JoinColumn
     * es el que tiene la FK en la base de datos.
     */
    // @JsonIgnore: rompe el ciclo Author ↔ AuthorProfile al serializar.
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false, unique = true)
    private Author author;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "twitter_handle")
    private String twitterHandle;

    @Column(name = "github_handle")
    private String githubHandle;

    @Column(name = "total_posts")
    private Integer totalPosts = 0;

    // Constructor vacío requerido por JPA
    protected AuthorProfile() {
    }

    public AuthorProfile(Author author) {
        this.author = author;
    }

    // Getters y setters

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getTwitterHandle() {
        return twitterHandle;
    }

    public void setTwitterHandle(String twitterHandle) {
        this.twitterHandle = twitterHandle;
    }

    public String getGithubHandle() {
        return githubHandle;
    }

    public void setGithubHandle(String githubHandle) {
        this.githubHandle = githubHandle;
    }

    public Integer getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(Integer totalPosts) {
        this.totalPosts = totalPosts;
    }
}
