package com.jester.server_loader_photo.photo.repo;

import com.jester.server_loader_photo.photo.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Optional<Photo> findByObjectName(String objectName);
}


