package com.example.demo.service.impl;

import com.example.demo.config.FileStorageProperties;
import com.example.demo.dto.response.VehicleDocumentResponse;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.Vehicle;
import com.example.demo.model.entity.VehicleDocument;
import com.example.demo.model.enums.DocumentType;
import com.example.demo.repository.VehicleDocumentRepository;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.service.FileStorageService;
import com.example.demo.service.VehicleDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class VehicleDocumentServiceImpl implements VehicleDocumentService {

    private static final Logger log = LoggerFactory.getLogger(VehicleDocumentServiceImpl.class);

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "jpg", "jpeg", "png", "webp"
    );

    private final VehicleRepository vehicleRepository;
    private final VehicleDocumentRepository vehicleDocumentRepository;
    private final FileStorageService fileStorageService;
    private final FileStorageProperties fileStorageProperties;

    public VehicleDocumentServiceImpl(VehicleRepository vehicleRepository,
                                      VehicleDocumentRepository vehicleDocumentRepository,
                                      FileStorageService fileStorageService,
                                      FileStorageProperties fileStorageProperties) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleDocumentRepository = vehicleDocumentRepository;
        this.fileStorageService = fileStorageService;
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public VehicleDocumentResponse uploadDocument(String vehicleId,
                                                  MultipartFile file,
                                                  DocumentType documentType,
                                                  LocalDate expiryDate) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        if (documentType == null) {
            throw new InvalidOperationException("Document type is required.");
        }

        validateFile(file);

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "document");
        String contentType = normalizeContentType(file.getContentType());

        String storedFileRef = fileStorageService.store(vehicleId, file);
        try {
            VehicleDocument document = new VehicleDocument();
            document.setVehicle(vehicle);
            document.setDocumentType(documentType);
            document.setOriginalFilename(originalFilename);
            document.setStoredFileRef(storedFileRef);
            document.setContentType(contentType);
            document.setFileSize(file.getSize());
            document.setExpiryDate(expiryDate);

            VehicleDocument saved = vehicleDocumentRepository.save(document);
            log.info("Uploaded document id={} vehicleId={} type={} file={}",
                    saved.getId(), vehicleId, documentType, originalFilename);
            return VehicleDocumentResponse.fromEntity(saved);
        } catch (RuntimeException ex) {
            fileStorageService.delete(storedFileRef);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleDocumentResponse> getDocumentsForVehicle(String vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle", "id", vehicleId);
        }
        return vehicleDocumentRepository.findByVehicleIdOrderByUploadDateDesc(vehicleId)
                .stream()
                .map(VehicleDocumentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedDocument downloadDocument(String vehicleId, String documentId) {
        VehicleDocument document = findDocumentForVehicle(vehicleId, documentId);
        Resource resource = fileStorageService.loadAsResource(document.getStoredFileRef());
        return new DownloadedDocument(resource, document.getContentType(), document.getOriginalFilename());
    }

    @Override
    public void deleteDocument(String vehicleId, String documentId) {
        VehicleDocument document = findDocumentForVehicle(vehicleId, documentId);
        String storedFileRef = document.getStoredFileRef();
        vehicleDocumentRepository.delete(document);
        fileStorageService.delete(storedFileRef);
        log.info("Deleted document id={} vehicleId={} ref={}", documentId, vehicleId, storedFileRef);
    }

    private VehicleDocument findDocumentForVehicle(String vehicleId, String documentId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle", "id", vehicleId);
        }
        return vehicleDocumentRepository.findByIdAndVehicleId(documentId, vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleDocument", "id", documentId));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidOperationException("Document file is required and cannot be empty.");
        }

        long maxBytes = fileStorageProperties.getMaxFileSizeBytes();
        if (file.getSize() > maxBytes) {
            throw new InvalidOperationException(
                    String.format("File size exceeds the maximum allowed limit of %d bytes.", maxBytes));
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidOperationException(
                    "Unsupported file extension. Allowed formats: PDF, JPG, JPEG, PNG, WEBP.");
        }

        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidOperationException(
                    "Unsupported file content type '" + contentType + "'. Allowed types: PDF, JPEG, PNG, WEBP.");
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        return contentType.trim().toLowerCase(Locale.ROOT);
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return null;
        }
        return filename.substring(dotIndex + 1).trim().toLowerCase(Locale.ROOT);
    }
}
