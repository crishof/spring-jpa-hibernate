-- Datos iniciales para que los endpoints tengan contenido desde el arranque

INSERT INTO authors (first_name, last_name, email, bio, address_city, address_country)
VALUES
  ('Martin', 'Fowler', 'mfowler@example.com',
   'Autor de Refactoring y Patterns of Enterprise Application Architecture',
   'Boston', 'USA'),
  ('Josh', 'Long', 'jlong@example.com',
   'Spring Developer Advocate en VMware Tanzu',
   'San Francisco', 'USA'),
  ('Venkat', 'Subramaniam', 'vsubramaniam@example.com',
   'Autor de Functional Programming in Java',
   'Houston', 'USA');

INSERT INTO author_profiles (author_id, twitter_handle, github_handle, total_posts)
VALUES
  (1, '@martinfowler', 'martinfowler', 12),
  (2, '@starbuxman', 'joshlong', 25),
  (3, '@venkat_s', 'venkats', 8);

INSERT INTO categories (name, description, parent_id) VALUES
  ('Programming', 'Artículos sobre programación en general', NULL),
  ('Java', 'Ecosistema Java y JVM', 1),
  ('Spring Framework', 'Spring Boot, Spring MVC, Spring Data', 2),
  ('Architecture', 'Patrones y arquitecturas de software', 1),
  ('DevOps', 'CI/CD, contenedores, infraestructura', NULL);

INSERT INTO tags (name, slug) VALUES
  ('java', 'java'), ('spring', 'spring'), ('hibernate', 'hibernate'),
  ('jpa', 'jpa'), ('patterns', 'patterns'), ('clean-code', 'clean-code'),
  ('microservices', 'microservices'), ('docker', 'docker');

INSERT INTO posts (title, slug, content, summary, status, author_id, category_id, published_at)
VALUES
  ('Introducción a JPA con Spring Boot',
   'introduccion-jpa-spring-boot',
   'JPA (Jakarta Persistence API) es el estándar para ORM en Java...',
   'Una guía completa para empezar con JPA y Spring Boot',
   'PUBLISHED', 1, 3, NOW() - INTERVAL '10 days'),
  ('Entendiendo el problema N+1 en Hibernate',
   'problema-n-plus-1-hibernate',
   'El problema N+1 es uno de los errores más comunes con Hibernate...',
   'Cómo detectar y resolver el infame problema N+1',
   'PUBLISHED', 2, 3, NOW() - INTERVAL '5 days'),
  ('Arquitectura hexagonal con Spring',
   'arquitectura-hexagonal-spring',
   'La arquitectura hexagonal separa el dominio de la infraestructura...',
   'Implementando Ports and Adapters en proyectos Spring',
   'DRAFT', 1, 4, NULL),
  ('Clean Architecture en proyectos Java',
   'clean-architecture-java',
   'Clean Architecture de Uncle Bob aplicada a proyectos Java reales...',
   'Guía práctica de Clean Architecture con Java',
   'PUBLISHED', 3, 4, NOW() - INTERVAL '2 days');

INSERT INTO post_tags (post_id, tag_id) VALUES
  (1, 1), (1, 2), (1, 3), (1, 4),
  (2, 1), (2, 3), (2, 4),
  (3, 2), (3, 5),
  (4, 1), (4, 5), (4, 6);

INSERT INTO comments (content, author_name, author_email, approved, post_id)
VALUES
  ('Excelente artículo, muy claro.', 'Carlos R.', 'carlos@example.com', true, 1),
  ('¿Hay algún repositorio con el código?', 'Ana M.', 'ana@example.com', true, 1),
  ('El problema N+1 me costó una semana de debugging.', 'Pedro L.', 'pedro@example.com', true, 2);

-- Respuesta anidada al primer comentario
INSERT INTO comments (content, author_name, author_email, approved, post_id, parent_id)
VALUES ('Sí, el repositorio está en GitHub.', 'Josh Long', 'jlong@example.com', true, 1, 1);
