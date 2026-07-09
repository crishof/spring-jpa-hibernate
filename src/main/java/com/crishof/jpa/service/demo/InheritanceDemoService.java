package com.crishof.jpa.service.demo;

import com.crishof.jpa.entity.inheritance.ContentItem;
import com.crishof.jpa.entity.inheritance.DraftContent;
import com.crishof.jpa.entity.inheritance.PageContent;
import com.crishof.jpa.repository.ContentItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * Demostración de herencia JOINED con consultas polimórficas.
 *
 * findAll() sobre ContentItem (la clase padre) devuelve instancias reales
 * de PageContent y DraftContent. Hibernate ejecuta los JOINs a las tablas
 * hijas y usa la columna discriminadora (dtype) para instanciar el tipo correcto.
 */
@Service
@Transactional(readOnly = true)
public class InheritanceDemoService {

    private final ContentItemRepository contentItemRepository;

    public InheritanceDemoService(ContentItemRepository contentItemRepository) {
        this.contentItemRepository = contentItemRepository;
    }

    // Consulta polimórfica: devuelve todas las subclases mezcladas
    public List<ContentItem> findAllContent() {
        return contentItemRepository.findAll();
    }

    /*
     * Clasifica cada ContentItem por su tipo concreto usando instanceof.
     * Demuestra que la consulta polimórfica preserva el tipo real de cada fila.
     */
    public Map<String, Long> countByConcreteType() {
        return contentItemRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        item -> item.getClass().getSimpleName(),
                        Collectors.counting()
                ));
    }

    // Describe cada item según su subclase (pattern matching de Java)
    public List<String> describeAll() {
        return contentItemRepository.findAll().stream()
                .map(item -> switch (item) {
                    case PageContent page ->
                            "PAGE: " + page.getTitle() + " → " + page.getUrlPath();
                    case DraftContent draft ->
                            "DRAFT: " + draft.getTitle() + " (auto-saves: " + draft.getAutoSaveCount() + ")";
                    default -> "UNKNOWN: " + item.getTitle();
                })
                .toList();
    }
}
