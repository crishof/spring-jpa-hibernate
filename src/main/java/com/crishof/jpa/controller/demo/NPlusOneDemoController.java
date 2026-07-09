package com.crishof.jpa.controller.demo;

import com.crishof.jpa.service.demo.NPlusOneDemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * Endpoints para demostrar el problema N+1 en un entorno real.
 * Revisar los logs de Hibernate para ver la diferencia en número de queries.
 */
@RestController
@RequestMapping("/api/demo/n-plus-one")
public class NPlusOneDemoController {

    private final NPlusOneDemoService demoService;

    public NPlusOneDemoController(NPlusOneDemoService demoService) {
        this.demoService = demoService;
    }

    // GET /api/demo/n-plus-one/problem
    // → ver en logs: múltiples "select author0_..." queries
    @GetMapping("/problem")
    public ResponseEntity<List<String>> demonstrateProblem() {
        return ResponseEntity.ok(demoService.demonstrateNPlusOneProblem());
    }

    // GET /api/demo/n-plus-one/join-fetch
    // → ver en logs: UN SOLO "select post0_... join author" query
    @GetMapping("/join-fetch")
    public ResponseEntity<List<String>> solveWithJoinFetch() {
        return ResponseEntity.ok(demoService.solveWithJoinFetch());
    }

    // GET /api/demo/n-plus-one/batch-fetch → explicación de batch_fetch_size
    @GetMapping("/batch-fetch")
    public ResponseEntity<String> explainBatchFetch() {
        return ResponseEntity.ok(demoService.explainBatchFetchSize());
    }
}
