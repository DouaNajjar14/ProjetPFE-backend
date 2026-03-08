package com.example.gestion.des.stagiaires.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file, String subDirectory) {
        try {
            Path uploadPath = Paths.get(uploadDir, subDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String newFilename = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return subDirectory + "/" + newFilename;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du fichier: " + e.getMessage());
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadDir, filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la suppression du fichier: " + e.getMessage());
        }
    }

    public Path getFilePath(String filePath) {
        return Paths.get(uploadDir, filePath);
    }
}
