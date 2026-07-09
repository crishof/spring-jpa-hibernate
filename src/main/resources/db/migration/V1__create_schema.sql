-- Migración 1: schema inicial del dominio blog
-- Convención de nombres Flyway: V{versión}__{descripción}.sql
-- Flyway ejecuta las migraciones en orden y registra en flyway_schema_history

-- Tabla base compartida: no existe como tabla (es @MappedSuperclass)
-- pero sus columnas se incluyen en cada tabla hija

-- Autores
CREATE TABLE authors (
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    bio         TEXT,
    -- Columnas de Address (@Embeddable): prefijo "address_"
    address_street   VARCHAR(200),
    address_city     VARCHAR(100),
    address_country  VARCHAR(100),
    address_zip_code VARCHAR(20),
    -- Columnas de auditoría (@MappedSuperclass)
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Perfil extendido del autor (@OneToOne)
CREATE TABLE author_profiles (
    id          BIGSERIAL PRIMARY KEY,
    author_id   BIGINT NOT NULL UNIQUE REFERENCES authors(id) ON DELETE CASCADE,
    website_url VARCHAR(255),
    twitter_handle VARCHAR(100),
    github_handle  VARCHAR(100),
    total_posts    INTEGER DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Categorías con auto-referencia (@ManyToOne self-join)
CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    -- FK a sí misma: categoría padre (puede ser NULL = categoría raíz)
    parent_id   BIGINT REFERENCES categories(id),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Tags (etiquetas para posts, @ManyToMany)
CREATE TABLE tags (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(50) NOT NULL UNIQUE,
    slug  VARCHAR(50) NOT NULL UNIQUE
);

-- Posts (artículo de blog — entidad central)
CREATE TABLE posts (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(300) NOT NULL,
    slug         VARCHAR(300) NOT NULL UNIQUE,
    content      TEXT NOT NULL,
    summary      VARCHAR(500),
    status       VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    -- @Version: columna de control de concurrencia optimista
    -- Hibernate incrementa este valor en cada UPDATE
    -- Si dos transacciones leen version=1 y ambas intentan UPDATE,
    -- la segunda falla con OptimisticLockException
    version      INTEGER NOT NULL DEFAULT 0,
    view_count   BIGINT NOT NULL DEFAULT 0,
    published_at TIMESTAMP,
    -- FK al autor (@ManyToOne)
    author_id    BIGINT NOT NULL REFERENCES authors(id),
    -- FK a la categoría (@ManyToOne)
    category_id  BIGINT REFERENCES categories(id),
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Índices para búsquedas frecuentes (coinciden con @Index en la entidad Post)
CREATE INDEX idx_post_slug ON posts (slug);
CREATE INDEX idx_post_author_status ON posts (author_id, status);

-- Tabla de unión Post ↔ Tag (@ManyToMany)
CREATE TABLE post_tags (
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    tag_id  BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);

-- Comentarios con auto-referencia (respuestas anidadas)
CREATE TABLE comments (
    id         BIGSERIAL PRIMARY KEY,
    content    TEXT NOT NULL,
    -- Nombre del autor del comentario (no es un User registrado)
    author_name  VARCHAR(100) NOT NULL,
    author_email VARCHAR(255),
    approved     BOOLEAN NOT NULL DEFAULT FALSE,
    -- FK al post al que pertenece el comentario
    post_id    BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    -- FK al comentario padre (auto-referencia para respuestas anidadas)
    parent_id  BIGINT REFERENCES comments(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Herencia JOINED: tabla padre para Content
-- Post, PageContent y DraftContent heredan de esta tabla
CREATE TABLE content_items (
    id           BIGSERIAL PRIMARY KEY,
    -- Columna discriminadora: qué tipo de Content es
    dtype        VARCHAR(50) NOT NULL,
    title        VARCHAR(300) NOT NULL,
    author_id    BIGINT NOT NULL REFERENCES authors(id),
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Tabla hija para estrategia JOINED: PageContent
CREATE TABLE page_contents (
    id          BIGINT PRIMARY KEY REFERENCES content_items(id),
    url_path    VARCHAR(300) NOT NULL UNIQUE,
    is_published BOOLEAN NOT NULL DEFAULT FALSE
);

-- Tabla hija para estrategia JOINED: DraftContent
CREATE TABLE draft_contents (
    id              BIGINT PRIMARY KEY REFERENCES content_items(id),
    last_saved_at   TIMESTAMP,
    auto_save_count INTEGER DEFAULT 0
);
