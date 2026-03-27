package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.exception.DocumentNotFoundException;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.candidatures}")
    private String bucketName;

    @Value("${app.upload.url-expiration-minutes}")
    private Integer urlExpirationMinutes;

    /**
     * Upload a file to MinIO storage
     * 
     * @param file   the file to upload
     * @param bucket the bucket name
     * @param key    the object key (path in bucket)
     * @return the object key
     */
    public String uploadFichier(MultipartFile file, String bucket, String key) {
        try {
            ensureBucketExists(bucket);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            return key;
        } catch (MinioException e) {
            throw new RuntimeException("Erreur MinIO lors de l'upload: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'upload: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a read-only presigned URL valid for the configured duration
     * Used for inline viewing (PDF viewer, etc.)
     * 
     * @param bucket the bucket name
     * @param key    the object key
     * @return the presigned URL
     */
    public String genererUrlLecture(String bucket, String key) {
        try {
            if (!fichierExiste(bucket, key)) {
                throw new DocumentNotFoundException("Fichier non trouvé: " + key);
            }

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(key)
                            .build());
        } catch (DocumentNotFoundException e) {
            throw e;
        } catch (MinioException e) {
            throw new RuntimeException("Erreur MinIO: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération de l'URL de lecture: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a download presigned URL valid for the configured duration
     * Sets Content-Disposition: attachment for browser download
     * 
     * @param bucket   the bucket name
     * @param key      the object key
     * @param fileName the original filename for download
     * @return the presigned URL
     */
    public String genererUrlTelechargement(String bucket, String key, String fileName) {
        try {
            if (!fichierExiste(bucket, key)) {
                throw new DocumentNotFoundException("Fichier non trouvé: " + key);
            }

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(key)
                            .extraQueryParams(java.util.Map.of(
                                    "response-content-disposition",
                                    "attachment; filename=\"" + fileName + "\""))
                            .build());
        } catch (DocumentNotFoundException e) {
            throw e;
        } catch (MinioException e) {
            throw new RuntimeException("Erreur MinIO: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération de l'URL de téléchargement: " + e.getMessage(), e);
        }
    }

    /**
     * Check if a file exists in MinIO
     * 
     * @param bucket the bucket name
     * @param key    the object key
     * @return true if file exists, false otherwise
     */
    public boolean fichierExiste(String bucket, String key) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build());
            return true;
        } catch (MinioException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la vérification du fichier: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a file from MinIO
     * 
     * @param bucket the bucket name
     * @param key    the object key
     */
    public void supprimerFichier(String bucket, String key) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build());
        } catch (MinioException e) {
            throw new RuntimeException("Erreur MinIO lors de la suppression: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression: " + e.getMessage(), e);
        }
    }

    /**
     * Ensure bucket exists, create if not
     * 
     * @param bucket the bucket name
     */
    private void ensureBucketExists(String bucket) {
        try {
            if (!minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build())) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (MinioException e) {
            throw new RuntimeException("Erreur MinIO lors de la vérification du bucket: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la gestion du bucket: " + e.getMessage(), e);
        }
    }
}
