package com.jester.server_loader_photo.photo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "photos")
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String objectName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long sizeBytes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}


