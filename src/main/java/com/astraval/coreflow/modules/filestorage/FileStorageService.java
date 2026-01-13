package com.astraval.coreflow.modules.filestorage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

@Service
public class FileStorageService {

    @Value("${app.file.upload.item-images}")
    private String itemImagePath;

    public FileStorage saveFile(MultipartFile file, String ownerType, String ownerId) throws IOException {
        // Create directory if not exists
        Path uploadPath = Paths.get(itemImagePath);
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
        fileStorage.setFileUrl("/uploads/item_img/" + storedName);
        fileStorage.setMimeType(file.getContentType());
        fileStorage.setFileSize(file.getSize());
        fileStorage.setChecksum(checksum);
        fileStorage.setStorageProvider("LOCAL");
        fileStorage.setIsPublic(true);
        fileStorage.setStatus("ACTIVE");

        return fileStorage;
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
}