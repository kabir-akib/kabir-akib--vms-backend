package com.example.demo.service;

import org.springframework.core.io.Resource;

public interface FileStorageService {

    String store(String vehicleId, MultipartFileWrapper file);

    Resource loadAsResource(String storedFileRef);

    void delete(String storedFileRef);

    /**
     * Lightweight wrapper or helper if needed; overload for Spring's MultipartFile.
     */
    default String store(String vehicleId, org.springframework.web.multipart.MultipartFile file) {
        return store(vehicleId, new MultipartFileWrapper(file));
    }

    final class MultipartFileWrapper {
        private final org.springframework.web.multipart.MultipartFile delegate;

        public MultipartFileWrapper(org.springframework.web.multipart.MultipartFile delegate) {
            this.delegate = delegate;
        }

        public org.springframework.web.multipart.MultipartFile getDelegate() {
            return delegate;
        }
    }
}
