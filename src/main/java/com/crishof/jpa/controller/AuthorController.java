package com.crishof.jpa.controller;

import com.crishof.jpa.dto.AuthorStatsDto;
import com.crishof.jpa.entity.Author;
import com.crishof.jpa.service.AuthorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * Endpoints REST de autores.
 */
@RestController
@RequestMapping("/api/v1/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    // GET /api/v1/authors → todos los autores
    @GetMapping
    public ResponseEntity<List<Author>> findAll() {
        return ResponseEntity.ok(authorService.findAll());
    }

    // GET /api/v1/authors/{id}/stats → estadísticas (class-based projection)
    @GetMapping("/{id}/stats")
    public ResponseEntity<AuthorStatsDto> getStats(@PathVariable Long id) {
        return ResponseEntity.ok(authorService.getStats(id));
    }
}
