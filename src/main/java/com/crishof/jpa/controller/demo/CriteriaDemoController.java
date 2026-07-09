package com.crishof.jpa.controller.demo;

import com.crishof.jpa.dto.PostSearchCriteria;
import com.crishof.jpa.entity.Post;
import com.crishof.jpa.enums.PostStatus;
import com.crishof.jpa.service.demo.CriteriaApiDemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/*
 * Endpoints para demostrar la Criteria API (búsqueda dinámica y agregación).
 */
@RestController
@RequestMapping("/api/demo/criteria")
public class CriteriaDemoController {

    private final CriteriaApiDemoService criteriaService;

    public CriteriaDemoController(CriteriaApiDemoService criteriaService) {
        this.criteriaService = criteriaService;
    }

    // GET /api/demo/criteria/search?title=jpa&status=PUBLISHED
    @GetMapping("/search")
    public ResponseEntity<List<Post>> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(required = false) Long categoryId) {
        PostSearchCriteria criteria = new PostSearchCriteria(title, status, categoryId, null);
        return ResponseEntity.ok(criteriaService.searchPosts(criteria));
    }

    // GET /api/demo/criteria/stats → conteo de posts por estado
    @GetMapping("/stats")
    public ResponseEntity<Map<PostStatus, Long>> countByStatus() {
        return ResponseEntity.ok(criteriaService.countPostsByStatus());
    }
}
