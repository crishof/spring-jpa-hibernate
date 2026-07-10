# Spring JPA & Hibernate

![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)
![Hibernate](https://img.shields.io/badge/Hibernate-7.4-59666C)
![JPA](https://img.shields.io/badge/Jakarta%20Persistence-3.2-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)
![Flyway](https://img.shields.io/badge/Flyway-12-CC0200)
![Maven](https://img.shields.io/badge/Maven-3.9-C71A36)

Proyecto académico (nº 05 de la serie) centrado en el mapeo objeto-relacional
con **JPA** e **Hibernate 7** sobre **Spring Boot 4.1.0**. Cubre relaciones,
herencia, JPQL, Criteria API, proyecciones, bloqueo optimista y el problema N+1.

## Descripción

**JPA (Jakarta Persistence API)** es la *especificación* estándar de Java para el
mapeo objeto-relacional (ORM): define anotaciones (`@Entity`, `@OneToMany`, …),
el lenguaje de consultas JPQL y la API del `EntityManager`. JPA por sí sola no
hace nada: es solo un contrato.

**Hibernate** es la *implementación* de referencia de esa especificación (el
"motor" ORM). Traduce las entidades y consultas JPQL a SQL concreto, gestiona el
contexto de persistencia (caché de primer nivel), el *dirty checking*, el *lazy
loading* y las estrategias de fetching.

En este proyecto usamos la **API estándar de JPA** siempre que es posible (para
mantener portabilidad) y bajamos a características específicas de Hibernate solo
cuando aportan valor (estadísticas, `batch_fetch_size`, native queries).

## Tecnologías

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 25 | Lenguaje (records, sealed, pattern matching en `switch`) |
| Spring Boot | 4.1.0 | Framework base y autoconfiguración |
| Hibernate ORM | 7.4.x | Implementación JPA |
| Jakarta Persistence | 3.2 | Especificación JPA |
| PostgreSQL | 16 | Base de datos de ejecución |
| Flyway | 12.x | Migraciones versionadas del schema |
| H2 | (gestionada) | Base de datos en memoria para tests |
| JUnit 5 + Mockito + AssertJ | (gestionadas) | Testing |

## Estructura del proyecto

```
src/
├── main/java/com/crishof/jpa/
│   ├── JpaHibernateApplication.java     # Punto de entrada
│   ├── config/
│   │   └── JpaAuditingConfig.java        # @EnableJpaAuditing (aislado para tests)
│   ├── entity/
│   │   ├── base/BaseEntity.java          # @MappedSuperclass + auditoría
│   │   ├── Address.java                   # @Embeddable (Value Object)
│   │   ├── Author.java                    # @Embedded + @OneToOne + @OneToMany
│   │   ├── AuthorProfile.java             # @OneToOne (lado propietario)
│   │   ├── Category.java                  # self-join @ManyToOne (árbol)
│   │   ├── Tag.java                       # @ManyToMany (lado inverso)
│   │   ├── Post.java                      # Entidad central: @Version, @Enumerated
│   │   ├── Comment.java                   # self-join (respuestas anidadas)
│   │   └── inheritance/                   # Herencia JOINED
│   │       ├── ContentItem.java           #   clase padre
│   │       ├── PageContent.java           #   subclase
│   │       └── DraftContent.java          #   subclase
│   ├── enums/PostStatus.java             # @Enumerated(STRING) + transiciones
│   ├── repository/                        # JPQL, JOIN FETCH, @EntityGraph, proyecciones
│   ├── service/
│   │   ├── PostService.java · AuthorService.java
│   │   └── demo/                          # Demos didácticas
│   │       ├── NPlusOneDemoService.java   #   problema N+1 y 3 soluciones
│   │       ├── CriteriaApiDemoService.java#   búsqueda dinámica Criteria API
│   │       ├── OptimisticLockDemoService.java # @Version
│   │       └── InheritanceDemoService.java#   consultas polimórficas
│   ├── controller/                        # Endpoints REST (+ demo/)
│   ├── dto/                               # Proyecciones (interface y record)
│   └── exception/GlobalExceptionHandler.java
├── main/resources/
│   ├── application.yml                    # PostgreSQL + Hibernate + Flyway
│   └── db/migration/                      # V1__create_schema.sql, V2__seed_data.sql
└── test/                                  # @DataJpaTest (H2) + Mockito
```

## Conceptos cubiertos

| Concepto | Clase / anotación | Descripción |
|----------|-------------------|-------------|
| `@MappedSuperclass` | `BaseEntity` | Campos comunes (id, auditoría) sin tabla propia |
| Auditoría JPA | `@CreatedDate` / `@LastModifiedDate` | Fechas rellenadas por Spring Data |
| `@Embeddable` / `@Embedded` | `Address` en `Author` | Value Object incrustado sin JOIN |
| `@OneToOne` LAZY | `Author` ↔ `AuthorProfile` | Bidireccional, lado propietario con `@JoinColumn` |
| `@OneToMany` / `@ManyToOne` | `Author` ↔ `Post` | Colección con `mappedBy` y cascada |
| `@ManyToMany` | `Post` ↔ `Tag` | `@JoinTable` con tabla de unión `post_tags` |
| Self-join | `Category`, `Comment` | Auto-referencia (árbol / respuestas anidadas) |
| `@Version` | `Post.version` | Bloqueo optimista (Optimistic Locking) |
| `@Enumerated(STRING)` | `Post.status` | Enum estable frente a reordenamientos |
| `@Inheritance(JOINED)` | `ContentItem` + subclases | Herencia con tabla padre + tablas hijas |
| JPQL + `JOIN FETCH` | `PostRepository` | Consulta orientada a entidades; resuelve N+1 |
| `@EntityGraph` | `findBySlugWithDetails` | Fetching declarativo por consulta |
| Proyección interface | `PostSummaryDto` | Proxy con solo los campos necesarios |
| Proyección clase (record) | `AuthorStatsDto` | Constructor JPQL `new ...` con agregación |
| Criteria API | `CriteriaApiDemoService` | Consultas dinámicas type-safe |
| `@Modifying` bulk update | `incrementViewCount` | UPDATE directo sin cargar la entidad |
| Native query | `fullTextSearch` | SQL puro (full-text search de PostgreSQL) |
| Problema N+1 | `NPlusOneDemoService` | Detección y 3 soluciones |

## Estrategias de herencia

JPA ofrece tres estrategias; aquí se implementa **JOINED**.

| Estrategia | Tablas | Pros | Contras | Cuándo usarla |
|------------|--------|------|---------|---------------|
| `SINGLE_TABLE` (default) | 1 tabla para toda la jerarquía | Sin JOINs, queries rápidas | Muchas columnas NULL; tabla enorme | Pocas subclases con campos similares |
| `JOINED` (este proyecto) | Tabla padre + 1 por subclase | Schema normalizado, sin NULLs, integridad | JOIN por cada consulta | Subclases con muchos campos propios |
| `TABLE_PER_CLASS` | 1 tabla completa por subclase | Consultas a subclase sin JOIN | Consultas polimórficas con `UNION ALL` (lento) | Casi nunca se consulta polimórficamente |

En el modelo: `content_items` (padre, con discriminador `dtype`) →
`page_contents` y `draft_contents` (hijas enlazadas por PK/FK).

## El problema N+1

El error de rendimiento más común con ORMs: cargas **N** entidades y luego
accedes a una relación LAZY de cada una, generando **1 + N** consultas.

### Cómo detectarlo
- Activar `hibernate.generate_statistics=true` (ya configurado).
- Revisar los logs SQL (`org.hibernate.SQL: DEBUG`): verás un `SELECT` inicial y
  luego un `SELECT` repetido por cada elemento.

### Tres soluciones

| Solución | Cómo | Cuándo |
|----------|------|--------|
| **JOIN FETCH** (JPQL) | `SELECT p FROM Post p JOIN FETCH p.author` | Consulta concreta que siempre necesita la relación |
| **`@EntityGraph`** | `@EntityGraph(attributePaths = {"author","category"})` | Reutilizable y declarativo; varias relaciones a la vez |
| **`default_batch_fetch_size`** | Config global (=20) | Sin tocar código: agrupa el lazy loading en `IN (…)` |

Compáralo en vivo con los endpoints `/api/demo/n-plus-one/problem` (N+1) y
`/api/demo/n-plus-one/join-fetch` (1 query), observando los logs SQL.

## Cómo ejecutar

### Requisitos
- Java 25, Docker (para PostgreSQL). Los tests **no** necesitan Docker (usan H2).

### Con PostgreSQL (aplicación completa)

```bash
# 1. Levantar PostgreSQL
docker compose up -d
docker compose ps            # esperar a que aparezca "healthy"

# 2. Arrancar la app (Flyway ejecuta V1 y V2 automáticamente al iniciar)
./mvnw spring-boot:run

# 3. Probar endpoints
curl -s http://localhost:8080/api/v1/posts | python3 -m json.tool
curl -s http://localhost:8080/api/demo/n-plus-one/problem | python3 -m json.tool
curl -s http://localhost:8080/api/demo/n-plus-one/join-fetch | python3 -m json.tool
curl -s "http://localhost:8080/api/demo/criteria/search?status=PUBLISHED" | python3 -m json.tool
curl -s http://localhost:8080/api/demo/criteria/stats | python3 -m json.tool

# 4. Detener PostgreSQL
docker compose down          # añade -v para borrar también el volumen
```

### Solo tests (H2 en memoria, sin Docker)

```bash
./mvnw test
```

### Endpoints principales

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/v1/posts` | Lista resumida (proyección interface) |
| GET | `/api/v1/posts/{id}` | Detalle con `JOIN FETCH` (author + category) |
| GET | `/api/v1/posts/by-slug/{slug}` | Detalle con `@EntityGraph` |
| POST | `/api/v1/posts/{id}/publish` | Transición de estado a PUBLISHED |
| POST | `/api/v1/posts/{id}/view` | Incremento de vistas (`@Modifying`) |
| GET | `/api/v1/authors` | Listado de autores |
| GET | `/api/v1/authors/{id}/stats` | Estadísticas (proyección record) |
| GET | `/api/demo/n-plus-one/{problem\|join-fetch\|batch-fetch}` | Demo N+1 |
| GET | `/api/demo/criteria/search?title=&status=&categoryId=` | Búsqueda dinámica |
| GET | `/api/demo/criteria/stats` | Conteo de posts por estado |
| GET | `/api/demo/inheritance/{all\|count-by-type\|describe}` | Consultas polimórficas |

## Serie de proyectos (portfolio)

| # | Proyecto | Descripción |
|---|----------|-------------|
| 01 | [java-oop-fundamentals](https://github.com/crishof/java-oop-fundamentals) | POO, generics, records, sealed classes, patrones |
| 02 | [java-collections-streams](https://github.com/crishof/java-collections-streams) | Collections Framework y Streams API |
| 03 | [spring-core-ioc](https://github.com/crishof/spring-core-ioc) | Spring Core, IoC, DI y AOP |
| 04 | [spring-rest-api](https://github.com/crishof/spring-rest-api) | REST API con Spring MVC y OpenAPI |
| 05 | [spring-jpa-hibernate](https://github.com/crishof/spring-jpa-hibernate) ← *este proyecto* | JPA, Hibernate, relaciones y caché |
| 06 | [spring-data-jpa](https://github.com/crishof/spring-data-jpa) | Spring Data JPA, paginación y specs |
| 07 | [spring-security-jwt](https://github.com/crishof/spring-security-jwt) | Spring Security, JWT y OAuth2 |
| 08 | [spring-testing](https://github.com/crishof/spring-testing) | Testing profesional con JUnit y Testcontainers |
| 09 | [spring-async](https://github.com/crishof/spring-async) | @Async, CompletableFuture y Scheduling |
| 10 | [spring-rabbitmq](https://github.com/crishof/spring-rabbitmq) | Mensajería con RabbitMQ y AMQP |
| 11 | [spring-kafka](https://github.com/crishof/spring-kafka) | Event streaming con Apache Kafka |
| 12 | [spring-docker](https://github.com/crishof/spring-docker) | Containerización con Docker y Compose |
| 13 | [spring-cicd](https://github.com/crishof/spring-cicd) | CI/CD con GitHub Actions |
| 14 | [ecommerce-layered-architecture](https://github.com/crishof/ecommerce-layered-architecture) | Monolito en capas (N-Tier) |
| 15 | [ecommerce-modular-monolith](https://github.com/crishof/ecommerce-modular-monolith) | Monolito modular por dominios |
| 16 | [ecommerce-hexagonal](https://github.com/crishof/ecommerce-hexagonal) | Arquitectura hexagonal (Ports & Adapters) |
| 17 | [ecommerce-clean-architecture](https://github.com/crishof/ecommerce-clean-architecture) | Clean Architecture (Uncle Bob) |
| 18 | [ecommerce-cqrs-event-sourcing](https://github.com/crishof/ecommerce-cqrs-event-sourcing) | CQRS y Event Sourcing |
| 19 | [ecommerce-microservices](https://github.com/crishof/ecommerce-microservices) | Microservicios con Spring Cloud |
| 20 | [ecommerce-saga-pattern](https://github.com/crishof/ecommerce-saga-pattern) | Saga Pattern y transacciones distribuidas |
| 21 | [ecommerce-observability](https://github.com/crishof/ecommerce-observability) | Observabilidad con Prometheus y Grafana |

## Notas de diseño (Hibernate 7 / Spring Boot 4)

- **Módulos separados en Boot 4**: la autoconfiguración de Flyway
  (`spring-boot-flyway`) y el slice `@DataJpaTest` (`spring-boot-data-jpa-test`)
  se extrajeron a módulos propios y deben declararse explícitamente.
- **`open-in-view: false`**: la sesión de Hibernate se cierra al salir del
  servicio. Las entidades expuestas por REST usan `@JsonIgnore` en sus
  relaciones LAZY para evitar `LazyInitializationException` y ciclos.
- **Auditoría en tests**: `@EnableJpaAuditing` está aislado en `JpaAuditingConfig`
  e importado en los `@DataJpaTest` con `@Import`, ya que el slice no carga la app.
- **Flyway es la única fuente del schema** en PostgreSQL (`ddl-auto: validate`);
  Hibernate solo valida. En tests, H2 crea el schema con `create-drop`.

---
_Autor: **Cristian Hoffmann** — Proyecto académico Java 25 / Spring Boot 4.1.0._
