package com.canvara.app.service;

import com.canvara.app.exception.FileStorageException;
import com.canvara.app.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
public class StorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_BYTES = 10L * 1024 * 1024; // 10 MB
    private static final String KEY_PREFIX = "";

    @Value("${canvara.s3.bucket}")
    private String bucket;

    @Value("${canvara.s3.region}")
    private String region;

    private S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .build(); // uses default credential chain: local ~/.aws, EC2 instance role
    }

    /**
     * Validates and uploads the file to S3.
     * @return stored filename (UUID-based), e.g. "a1b2c3d4.jpg"
     */
    public String store(MultipartFile file) {
        validate(file);

        String originalName   = StringUtils.cleanPath(file.getOriginalFilename());
        String extension      = getExtension(originalName);
        String storedFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String s3Key          = KEY_PREFIX + storedFilename;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client().putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (Exception ex) {
            throw new FileStorageException("Failed to upload file to S3: " + storedFilename, ex);
        }

        return storedFilename;
    }

    /** Returns the public S3 URL for a stored filename. */
    public String resolveUrl(String filename) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + KEY_PREFIX + filename;
    }

    /** Deletes a file from S3 (best-effort). */
    public void delete(String filename) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(KEY_PREFIX + filename)
                    .build();
            s3Client().deleteObject(request);
        } catch (Exception ex) {
            System.err.println("Warning: could not delete S3 object " + filename + ": " + ex.getMessage());
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File must not be empty");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new InvalidFileException("File size exceeds 10 MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new InvalidFileException("Only JPEG, PNG and WEBP images are allowed");
        }
        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalName.contains("..")) {
            throw new InvalidFileException("Invalid filename: " + originalName);
        }
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "jpg";
        return filename.substring(dot + 1).toLowerCase();
    }
}
