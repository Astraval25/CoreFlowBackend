package com.astraval.coreflow.modules.filestorage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

@Service
public class FileStorageService {

    @Value("${app.file.upload.item-images}")
    private String itemImagePath;

    @Value("${app.file.upload.payment-proofs:uploads/payment_proof}")
    private String paymentProofPath;

    private final FileStorageRepository repo;

    public FileStorageService(FileStorageRepository repo) {
        this.repo = repo;
    }

    public FileStorage saveFile(MultipartFile file, String ownerType, String ownerId) throws IOException {
        // Create directory if not exists
        Path uploadPath = resolveUploadPath(ownerType);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalName = file.getOriginalFilename();
        String storedName = java.util.UUID.randomUUID().toString() + "_" + originalName;
        Path filePath = uploadPath.resolve(storedName);

        // Save file
        Files.copy(file.getInputStream(), filePath);

        // Calculate checksum
        String checksum = calculateChecksum(file.getBytes());

        // Create FileStorage entity
        FileStorage fileStorage = new FileStorage();
        fileStorage.setFsId(java.util.UUID.randomUUID().toString());
        fileStorage.setOwnerType(ownerType);
        fileStorage.setOwnerId(ownerId);
        fileStorage.setOriginalName(originalName);
        fileStorage.setStoredName(storedName);
        fileStorage.setFilePath(filePath.toString());
        fileStorage.setFileUrl(uploadPath.toString() + "/" + storedName);
        String mime = file.getContentType();
        if (mime == null || !mime.contains("/")) {
            mime = "application/octet-stream";
        }
        fileStorage.setMimeType(mime);
        fileStorage.setFileSize(file.getSize());
        fileStorage.setChecksum(checksum);
        fileStorage.setStorageProvider("LOCAL");
        fileStorage.setIsPublic(true);
        fileStorage.setStatus("ACTIVE");

        return fileStorage;
    }

    private Path resolveUploadPath(String ownerType) {
        if (ownerType != null && ownerType.equalsIgnoreCase("PAYMENT_PROOF")) {
            return Paths.get(paymentProofPath);
        }
        return Paths.get(itemImagePath);
    }

    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public ResponseEntity<Resource> getFileObject(String fsId) {

        FileStorage fs = repo.findByFsId(fsId)
                .orElseThrow(() -> new RuntimeException("File metadata not found"));

        try {
            Path path = Paths.get(fs.getFilePath());
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("File missing on disk");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fs.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + fs.getOriginalName() + "\"")
                    .contentLength(fs.getFileSize())
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid file path", e);
        }
    }
}
