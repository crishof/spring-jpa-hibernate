package com.crishof.jpa.entity.inheritance;

import com.crishof.jpa.entity.Author;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/*
 * Subclase de ContentItem (estrategia JOINED).
 * Sus columnas propias viven en la tabla page_contents, enlazada por PK
 * con content_items. @DiscriminatorValue("PAGE") identifica las filas.
 */
@Entity
@Table(name = "page_contents")
@DiscriminatorValue("PAGE")
public class PageContent extends ContentItem {

    @Column(name = "url_path", nullable = false, unique = true)
    private String urlPath;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    // Constructor vacío requerido por JPA
    protected PageContent() {
    }

    public PageContent(String title, Author author, String urlPath) {
        super(title, author);
        this.urlPath = urlPath;
    }

    // Getters y setters

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }
}
