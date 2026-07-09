package com.crishof.jpa.controller;

import com.crishof.jpa.dto.PostSummaryDto;
import com.crishof.jpa.entity.Post;
import com.crishof.jpa.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * Endpoints REST de posts.
 * Cada endpoint demuestra una técnica JPA distinta (projection, JOIN FETCH,
 * @EntityGraph, transición de estado, bulk update).
 */
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // GET /api/v1/posts → lista resumida (interface-based projection)
    @GetMapping
    public ResponseEntity<List<PostSummaryDto>> findAll() {
        return ResponseEntity.ok(postService.findAllSummaries());
    }

    // GET /api/v1/posts/{id} → detalle completo con JOIN FETCH
    @GetMapping("/{id}")
    public ResponseEntity<Post> findById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.findByIdWithDetails(id));
    }

    // GET /api/v1/posts/by-slug/{slug} → por slug con @EntityGraph
    @GetMapping("/by-slug/{slug}")
    public ResponseEntity<Post> findBySlug(@PathVariable String slug) {
        return postService.findBySlug(slug)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/v1/posts/{id}/publish → transición de estado
    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable Long id) {
        postService.publish(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/v1/posts/{id}/view → incrementar contador (bulk update)
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> registerView(@PathVariable Long id) {
        postService.registerView(id);
        return ResponseEntity.noContent().build();
    }
}
