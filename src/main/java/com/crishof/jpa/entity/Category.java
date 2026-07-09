package com.crishof.jpa.entity;

import com.crishof.jpa.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/*
 * Self-referencing @ManyToOne: una categoría puede tener una categoría padre.
 * Esto crea una estructura de árbol donde:
 * - Nodos raíz: parent == null
 * - Nodos hoja: children.isEmpty()
 *
 * Ejemplo de jerarquía:
 * Programming (raíz)
 *   └── Java (hijo)
 *         └── Spring Framework (nieto)
 */
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /*
     * @ManyToOne self-join: la categoría padre.
     * nullable = true → categorías raíz no tienen padre.
     * FetchType.LAZY: no cargar el padre automáticamente.
     */
    // @JsonIgnore: relación LAZY; evita lazy loading al serializar y ciclos.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /*
     * @OneToMany self-join: las categorías hijas.
     * mappedBy = "parent": la FK está en la columna parent_id de Category.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Category> children = new ArrayList<>();

    // Constructor vacío requerido por JPA
    protected Category() {
    }

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Método de conveniencia para construir el árbol manteniendo la bidireccionalidad
    public void addChild(Category child) {
        children.add(child);
        child.setParent(this);
    }

    // ¿Es una categoría raíz? (sin padre)
    // @JsonIgnore: método derivado; Jackson lo detectaría como propiedad "root"
    // y accedería a la relación LAZY 'parent' al serializar.
    @JsonIgnore
    public boolean isRoot() {
        return parent == null;
    }

    // ¿Es una categoría hoja? (sin hijas)
    // @JsonIgnore: sin esto Jackson serializaría la propiedad "leaf" invocando
    // children.isEmpty(), lo que dispara LazyInitializationException fuera de sesión.
    @JsonIgnore
    public boolean isLeaf() {
        return children.isEmpty();
    }

    // Getters y setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public List<Category> getChildren() {
        return children;
    }
}
