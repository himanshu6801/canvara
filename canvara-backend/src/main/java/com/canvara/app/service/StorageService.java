package com.canvara.app.service;

import com.canvara.app.exception.FileStorageException;
import com.canvara.app.exception.InvalidFileException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class StorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_BYTES = 10L * 1024 * 1024; // 10 MB

    @Value("${canvara.upload.dir}")
    private String uploadDir;

    @Value("${canvara.upload.base-url}")
    private String baseUrl;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create upload directory: " + uploadPath, ex);
        }
    }

    /**
     * Validates and saves the file.
     * @return stored filename (UUID-based), e.g. "a1b2c3d4.jpg"
     */
    public String store(MultipartFile file) {
        validate(file);

        String originalName  = StringUtils.cleanPath(file.getOriginalFilename());
        String extension     = getExtension(originalName);
        String storedFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        Path destination = uploadPath.resolve(storedFilename).normalize();

        // Security: prevent path traversal
        if (!destination.startsWith(uploadPath)) {
            throw new InvalidFileException("Cannot store file outside designated directory");
        }

        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to store file: " + storedFilename, ex);
        }

        return storedFilename;
    }

    /** Resolves a stored filename to its full public URL. */
    public String resolveUrl(String filename) {
        return baseUrl + "/" + filename;
    }

    /** Deletes a previously stored file (best-effort; logs if missing). */
    public void delete(String filename) {
        try {
            Path file = uploadPath.resolve(filename).normalize();
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            // Non-fatal: log but don't throw
            System.err.println("Warning: could not delete file " + filename + ": " + ex.getMessage());
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
