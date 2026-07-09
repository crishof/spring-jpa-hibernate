package com.crishof.jpa.repository;

import com.crishof.jpa.config.JpaAuditingConfig;
import com.crishof.jpa.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Tests de la jerarquía auto-referenciada de Category (self-join @ManyToOne).
 */
@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("CategoryRepository — Tests de jerarquía self-join")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category root;
    private Category child;

    @BeforeEach
    void setUp() {
        // Programming (raíz) → Java (hijo)
        root = new Category("Programming", "Categoría raíz");
        Category java = new Category("Java", "Ecosistema Java");
        root.addChild(java);

        categoryRepository.save(root); // sin cascade explícito guardamos ambas manualmente
        categoryRepository.save(java);
        this.child = java;

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findRootCategories() debe retornar solo categorías sin padre")
    void should_return_only_root_categories() {
        List<Category> roots = categoryRepository.findRootCategories();

        assertThat(roots).hasSize(1);
        assertThat(roots.get(0).getName()).isEqualTo("Programming");
        assertThat(roots.get(0).isRoot()).isTrue();
    }

    @Test
    @DisplayName("findByParentId() debe retornar las categorías hijas de un padre")
    void should_return_children_of_parent() {
        List<Category> children = categoryRepository.findByParentId(root.getId());

        assertThat(children).hasSize(1);
        assertThat(children.get(0).getName()).isEqualTo("Java");
    }

    @Test
    @DisplayName("La categoría hija debe referenciar a su padre")
    void should_link_child_to_parent() {
        Category reloaded = categoryRepository.findById(child.getId()).orElseThrow();

        assertThat(reloaded.getParent()).isNotNull();
        assertThat(reloaded.getParent().getName()).isEqualTo("Programming");
        assertThat(reloaded.isRoot()).isFalse();
    }
}
