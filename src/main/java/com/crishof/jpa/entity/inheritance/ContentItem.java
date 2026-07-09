package com.crishof.jpa.entity.inheritance;

import com.crishof.jpa.entity.Author;
import com.crishof.jpa.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/*
 * ESTRATEGIAS DE HERENCIA EN JPA:
 *
 * 1. SINGLE_TABLE (default): todas las subclases en UNA tabla.
 *    Pro: mejor rendimiento (sin JOINs), queries simples.
 *    Con: columnas de subclases pueden ser NULL; tabla crece mucho.
 *    Usar cuando: pocas subclases con campos similares.
 *
 * 2. JOINED (este ejemplo): tabla padre + tabla por subclase.
 *    Pro: schema normalizado, sin columnas NULL innecesarias.
 *    Con: JOIN requerido para cada consulta.
 *    Usar cuando: subclases tienen muchos campos propios, integridad importante.
 *
 * 3. TABLE_PER_CLASS: una tabla completa por cada subclase.
 *    Pro: queries a subclase sin JOINs.
 *    Con: queries polimórficas usan UNION ALL (lento).
 *    Usar cuando: casi nunca consultas polimórficas.
 *
 * Clase padre con estrategia JOINED: la tabla content_items contiene las
 * columnas comunes; cada subclase tiene su propia tabla enlazada por PK/FK.
 */
@Entity
@Table(name = "content_items")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
public abstract class ContentItem extends BaseEntity {

    @Column(nullable = false, length = 300)
    private String title;

    // @JsonIgnore: relación LAZY; evita lazy loading al serializar consultas
    // polimórficas fuera de sesión (open-in-view=false).
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    // Constructor vacío requerido por JPA
    protected ContentItem() {
    }

    protected ContentItem(String title, Author author) {
        this.title = title;
        this.author = author;
    }

    // Getters y setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
}
