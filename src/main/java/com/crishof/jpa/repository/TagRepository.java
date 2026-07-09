package com.crishof.jpa.repository;

import com.crishof.jpa.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
 * Repositorio de tags (lado inverso del @ManyToMany con Post).
 */
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findBySlug(String slug);

    Optional<Tag> findByName(String name);
}
