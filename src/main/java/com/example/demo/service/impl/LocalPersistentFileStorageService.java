package com.example.demo.service.impl;

import com.example.demo.config.FileStorageProperties;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalPersistentFileStorageService implements FileStorageService {

    private final Path rootLocation;

    public LocalPersistentFileStorageService(FileStorageProperties properties) {
        this.rootLocation = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not initialize file storage directory: " + this.rootLocation, ex);
        }
    }

    @Override
    public String store(String vehicleId, MultipartFileWrapper wrapper) {
        MultipartFile file = wrapper.getDelegate();
        String safeVehicleDir = sanitizeSegment(vehicleId);
        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "document");
        String safeFilename = sanitizeFilename(originalFilename);
        String storedFilename = UUID.randomUUID() + "-" + safeFilename;

        Path vehicleDir = resolveInsideRoot(safeVehicleDir);
        Path targetFile = resolveInsideRoot(safeVehicleDir + "/" + storedFilename);

        try {
            Files.createDirectories(vehicleDir);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return safeVehicleDir + "/" + storedFilename;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store uploaded file '" + originalFilename + "'", ex);
        }
    }

    @Override
    public Resource loadAsResource(String storedFileRef) {
        try {
            Path filePath = resolveInsideRoot(storedFileRef);
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Stored document file not found: " + storedFileRef);
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Stored document file could not be read: " + storedFileRef);
        }
    }

    @Override
    public void delete(String storedFileRef) {
        if (storedFileRef == null || storedFileRef.isBlank()) {
            return;
        }
        try {
            Path filePath = resolveInsideRoot(storedFileRef);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete stored file: " + storedFileRef, ex);
        }
    }

    private Path resolveInsideRoot(String relativePath) {
        Path resolved = this.rootLocation.resolve(relativePath).normalize();
        if (!resolved.startsWith(this.rootLocation)) {
            throw new InvalidOperationException("Invalid file storage path reference.");
        }
        return resolved;
    }

    private String sanitizeSegment(String segment) {
        String cleaned = segment == null ? "unknown" : segment.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (cleaned.isBlank() || cleaned.equals(".") || cleaned.equals("..")) {
            return "unknown";
        }
        return cleaned;
    }

    private String sanitizeFilename(String filename) {
        Path baseName = Paths.get(filename).getFileName();
        String name = baseName != null ? baseName.toString() : "document";
        String cleaned = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        return cleaned.isBlank() ? "document" : cleaned;
    }
}
