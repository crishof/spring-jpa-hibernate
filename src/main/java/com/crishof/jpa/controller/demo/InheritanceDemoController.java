package com.crishof.jpa.controller.demo;

import com.crishof.jpa.entity.inheritance.ContentItem;
import com.crishof.jpa.service.demo.InheritanceDemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/*
 * Endpoints para demostrar consultas polimórficas sobre herencia JOINED.
 */
@RestController
@RequestMapping("/api/demo/inheritance")
public class InheritanceDemoController {

    private final InheritanceDemoService inheritanceService;

    public InheritanceDemoController(InheritanceDemoService inheritanceService) {
        this.inheritanceService = inheritanceService;
    }

    // GET /api/demo/inheritance/all → todos los ContentItem (polimórfico)
    @GetMapping("/all")
    public ResponseEntity<List<ContentItem>> findAll() {
        return ResponseEntity.ok(inheritanceService.findAllContent());
    }

    // GET /api/demo/inheritance/count-by-type → conteo por subclase concreta
    @GetMapping("/count-by-type")
    public ResponseEntity<Map<String, Long>> countByType() {
        return ResponseEntity.ok(inheritanceService.countByConcreteType());
    }

    // GET /api/demo/inheritance/describe → descripción textual por tipo
    @GetMapping("/describe")
    public ResponseEntity<List<String>> describe() {
        return ResponseEntity.ok(inheritanceService.describeAll());
    }
}
