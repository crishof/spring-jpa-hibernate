package com.crishof.jpa.repository;

import com.crishof.jpa.entity.inheritance.ContentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
 * Repositorio polimórfico sobre la jerarquía JOINED de ContentItem.
 *
 * findAll() sobre la clase padre devuelve TODAS las subclases
 * (PageContent y DraftContent) con los JOINs correspondientes a cada tabla hija.
 * Es una consulta polimórfica: Hibernate resuelve el tipo real vía discriminador.
 */
public interface ContentItemRepository extends JpaRepository<ContentItem, Long> {

    List<ContentItem> findByAuthorId(Long authorId);
}
