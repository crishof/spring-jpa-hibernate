package com.crishof.jpa.entity;

import com.crishof.jpa.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/*
 * Entidad Author: demuestra @Embedded y @OneToOne bidireccional.
 *
 * REGLA DE CASCADA:
 * CascadeType.ALL en profile: cuando guardamos un Author, Spring
 * guarda automáticamente su AuthorProfile asociado.
 * Cuando eliminamos el Author, también se elimina el profile.
 *
 * orphanRemoval = true: si desasociamos el profile del author
 * (author.setProfile(null)), Hibernate eliminará el profile huérfano.
 */
@Entity
@Table(name = "authors")
public class Author extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String bio;

    /*
     * @Embedded: incrusta los campos de Address directamente en la tabla authors.
     * No genera un JOIN: las columnas (address_city, address_country, etc.)
     * son parte de la misma fila de la tabla authors.
     */
    @Embedded
    private Address address;

    /*
     * @OneToOne bidireccional:
     * - AuthorProfile es el lado propietario (tiene @JoinColumn)
     * - Author es el lado inverso (tiene mappedBy)
     *
     * fetch = LAZY: Hibernate NO carga el profile a menos que se acceda.
     * Si fuera EAGER (default en @OneToOne), cada SELECT de Author
     * generaría automáticamente un JOIN o una segunda query para el profile.
     *
     * CascadeType.ALL: guardar/eliminar Author hace lo mismo al profile.
     */
    // @JsonIgnore: relación LAZY; evita LazyInitializationException al serializar
    // el Author fuera de sesión (open-in-view=false) y rompe ciclos de referencia.
    @JsonIgnore
    @OneToOne(mappedBy = "author",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private AuthorProfile profile;

    /*
     * @OneToMany bidireccional hacia Post.
     * mappedBy = "author": indica que Post tiene la FK (author_id).
     * Sin mappedBy, Hibernate crearía una tabla de unión innecesaria.
     *
     * CascadeType.ALL con orphanRemoval: eliminar un post de esta lista
     * también lo elimina de la base de datos.
     *
     * FetchType.LAZY (obligatorio en @OneToMany):
     * EAGER en colecciones es casi siempre un error de rendimiento.
     */
    // @JsonIgnore: colección LAZY; evita serializar posts (y el ciclo
    // Post → author → posts) cuando el Author se devuelve como JSON.
    @JsonIgnore
    @OneToMany(mappedBy = "author",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    @OrderBy("createdAt DESC")  // orden por defecto al cargar la colección
    private List<Post> posts = new ArrayList<>();

    // Constructor vacío requerido por JPA
    protected Author() {
    }

    // Constructor para crear Author con campos obligatorios
    public Author(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    // Método de conveniencia: añade un post y mantiene la bidireccionalidad
    public void addPost(Post post) {
        posts.add(post);
        post.setAuthor(this);  // sincronizar el lado propietario de la relación
    }

    // Método de conveniencia: elimina un post manteniendo la coherencia
    public void removePost(Post post) {
        posts.remove(post);
        post.setAuthor(null);
    }

    // Método de conveniencia para asociar el profile de forma bidireccional
    public void setProfile(AuthorProfile profile) {
        this.profile = profile;
        if (profile != null) {
            profile.setAuthor(this);
        }
    }

    // Getters y setters

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public AuthorProfile getProfile() {
        return profile;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // toString() SIN incluir 'posts' ni 'profile' para evitar recursión infinita
    @Override
    public String toString() {
        return "Author{id=" + getId() + ", firstName='" + firstName + '\''
                + ", lastName='" + lastName + '\'' + ", email='" + email + '\'' + '}';
    }
}
