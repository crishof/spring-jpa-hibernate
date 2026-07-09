package com.crishof.jpa.service;

import com.crishof.jpa.dto.AuthorStatsDto;
import com.crishof.jpa.entity.Author;
import com.crishof.jpa.repository.AuthorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * Servicio de autores: gestión y estadísticas.
 */
@Service
@Transactional(readOnly = true)
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public List<Author> findAll() {
        return authorRepository.findAll();
    }

    // Estadísticas del autor mediante class-based projection (record + JPQL new)
    public AuthorStatsDto getStats(Long authorId) {
        return authorRepository.findAuthorStats(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Autor no encontrado: " + authorId));
    }

    // Autor con su profile cargado (JOIN FETCH del @OneToOne LAZY)
    public Author findByIdWithProfile(Long id) {
        return authorRepository.findByIdWithProfile(id)
                .orElseThrow(() -> new EntityNotFoundException("Autor no encontrado: " + id));
    }

    /*
     * Crear un autor validando que el email no exista previamente.
     * @Transactional (escritura): persiste el autor y, por cascada,
     * su profile asociado si lo tuviera.
     */
    @Transactional
    public Author create(Author author) {
        if (authorRepository.existsByEmail(author.getEmail())) {
            throw new IllegalArgumentException("Ya existe un autor con el email: " + author.getEmail());
        }
        return authorRepository.save(author);
    }
}
