package com.crishof.jpa.repository;

import com.crishof.jpa.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/*
 * Repositorio de categorías. Incluye consultas sobre la jerarquía
 * auto-referenciada (categorías raíz vs hijas).
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    // Categorías raíz: sin padre (parent IS NULL)
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL")
    List<Category> findRootCategories();

    // Categorías hijas directas de un padre dado
    List<Category> findByParentId(Long parentId);
}
