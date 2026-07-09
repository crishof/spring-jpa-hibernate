package com.crishof.jpa.entity.inheritance;

import com.crishof.jpa.entity.Author;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/*
 * Subclase de ContentItem (estrategia JOINED).
 * Representa contenido en borrador con metadatos de auto-guardado.
 * @DiscriminatorValue("DRAFT") identifica sus filas en content_items.
 */
@Entity
@Table(name = "draft_contents")
@DiscriminatorValue("DRAFT")
public class DraftContent extends ContentItem {

    @Column(name = "last_saved_at")
    private LocalDateTime lastSavedAt;

    @Column(name = "auto_save_count")
    private Integer autoSaveCount = 0;

    // Constructor vacío requerido por JPA
    protected DraftContent() {
    }

    public DraftContent(String title, Author author) {
        super(title, author);
    }

    // Registra un auto-guardado: incrementa el contador y sella la fecha
    public void autoSave() {
        this.autoSaveCount++;
        this.lastSavedAt = LocalDateTime.now();
    }

    // Getters y setters

    public LocalDateTime getLastSavedAt() {
        return lastSavedAt;
    }

    public void setLastSavedAt(LocalDateTime lastSavedAt) {
        this.lastSavedAt = lastSavedAt;
    }

    public Integer getAutoSaveCount() {
        return autoSaveCount;
    }

    public void setAutoSaveCount(Integer autoSaveCount) {
        this.autoSaveCount = autoSaveCount;
    }
}
