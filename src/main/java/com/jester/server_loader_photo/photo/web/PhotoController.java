package com.jester.server_loader_photo.photo.web;

import com.jester.server_loader_photo.photo.model.Photo;
import com.jester.server_loader_photo.photo.repo.PhotoRepository;
import com.jester.server_loader_photo.photo.service.PhotoStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/photos")
@Validated
public class PhotoController {

    private final PhotoStorageService photoStorageService;
    private final PhotoRepository photoRepository;

    public PhotoController(PhotoStorageService photoStorageService, PhotoRepository photoRepository) {
        this.photoStorageService = photoStorageService;
        this.photoRepository = photoRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoResponse> upload(@RequestPart("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Photo saved = photoStorageService.uploadToMinio(file);
        String url = photoStorageService.presignGetUrl(saved.getObjectName(), Duration.ofMinutes(30));
        return ResponseEntity.ok(PhotoResponse.from(saved, url));
    }

    @GetMapping
    public List<PhotoResponse> list() {
        return photoRepository.findAll().stream()
                .map(p -> PhotoResponse.from(p, null))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/url")
    public ResponseEntity<String> presign(@PathVariable Long id) throws Exception {
        return photoRepository.findById(id)
                .map(p -> {
                    try {
                        String url = photoStorageService.presignGetUrl(p.getObjectName(), Duration.ofMinutes(30));
                        return ResponseEntity.ok(url);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record PhotoResponse(Long id, String objectName, String contentType, Long sizeBytes, String url) {
        static PhotoResponse from(Photo p, String url) {
            return new PhotoResponse(p.getId(), p.getObjectName(), p.getContentType(), p.getSizeBytes(), url);
        }
    }
}


