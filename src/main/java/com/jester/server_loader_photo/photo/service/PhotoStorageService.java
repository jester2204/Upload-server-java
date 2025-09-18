package com.jester.server_loader_photo.photo.service;

import com.jester.server_loader_photo.photo.model.Photo;
import com.jester.server_loader_photo.photo.repo.PhotoRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
public class PhotoStorageService {

    private final MinioClient minioClient;
    private final PhotoRepository photoRepository;
    private final String bucket;

    public PhotoStorageService(MinioClient minioClient,
                               PhotoRepository photoRepository,
                               @Value("${minio.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.photoRepository = photoRepository;
        this.bucket = bucket;
    }

    public Photo uploadToMinio(MultipartFile file) throws Exception {
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        String extension = guessExtension(file.getOriginalFilename());
        String objectName = UUID.randomUUID().toString() + (extension.isEmpty() ? "" : ("." + extension));

        try (InputStream input = file.getInputStream()) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .contentType(contentType)
                    .stream(input, file.getSize(), -1)
                    .build();

            minioClient.putObject(args);
        }

        Photo p = new Photo();
        p.setObjectName(objectName);
        p.setContentType(contentType);
        p.setSizeBytes(file.getSize());
        return photoRepository.save(p);
    }

    public String presignGetUrl(String objectName, Duration ttl) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .method(Method.GET)
                        .expiry((int) ttl.toSeconds())
                        .build()
        );
    }

    private String guessExtension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        if (idx == -1) return "";
        return filename.substring(idx + 1);
    }
}


