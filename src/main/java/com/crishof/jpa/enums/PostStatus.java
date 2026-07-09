package com.crishof.jpa.enums;

/**
 * Estado del ciclo de vida de un Post.
 * Se almacena como STRING en la BD (ver @Enumerated(EnumType.STRING) en Post).
 */
public enum PostStatus {
    DRAFT("Borrador"),
    PUBLISHED("Publicado"),
    ARCHIVED("Archivado");

    private final String displayName;

    PostStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Transiciones válidas: qué estado puede seguir a este
    public boolean canTransitionTo(PostStatus next) {
        return switch (this) {
            case DRAFT -> next == PUBLISHED;
            case PUBLISHED -> next == ARCHIVED;
            case ARCHIVED -> false; // estado terminal
        };
    }
}
