package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.DocumentUrlResponse;
import com.example.gestion.des.stagiaires.entity.Candidat;
import com.example.gestion.des.stagiaires.exception.DocumentAccessDeniedException;
import com.example.gestion.des.stagiaires.exception.DocumentNotFoundException;
import com.example.gestion.des.stagiaires.exception.InvalidFileException;
import com.example.gestion.des.stagiaires.repository.CandidatRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentCandidatureService {

    private final MinioService minioService;
    private final CandidatRepository candidatRepository;
    private final Tika tika = new Tika();

    @Value("${minio.bucket.candidatures}")
    private String bucketName;

    @Value("${app.upload.allowed-file-size}")
    private long maxFileSize;

    @Value("${app.upload.url-expiration-minutes}")
    private Integer urlExpirationMinutes;

    /**
     * Upload CV for a candidate
     * 
     * @param candidatId the candidate UUID
     * @param file       the PDF file to upload
     * @return DocumentUrlResponse with upload details
     */
    @Transactional
    public DocumentUrlResponse uploadCv(UUID candidatId, MultipartFile file) {
        Candidat candidat = getCandidatOrThrow(candidatId);

        // Validate file
        validateFile(file);

        // Upload to MinIO
        String key = "candidatures/" + candidatId + "/cv.pdf";
        String uploadedKey = minioService.uploadFichier(file, bucketName, key);

        // Update candidate in database
        candidat.setCv(uploadedKey);
        candidatRepository.save(candidat);

        return DocumentUrlResponse.builder()
                .id(uploadedKey)
                .nomDocument("CV")
                .type("application/pdf")
                .tailleBytes(file.getSize())
                .expiresDans(urlExpirationMinutes + " minutes")
                .build();
    }

    /**
     * Upload lettre de motivation for a candidate
     * 
     * @param candidatId the candidate UUID
     * @param file       the PDF file to upload
     * @return DocumentUrlResponse with upload details
     */
    @Transactional
    public DocumentUrlResponse uploadLettreMotivation(UUID candidatId, MultipartFile file) {
        Candidat candidat = getCandidatOrThrow(candidatId);

        // Validate file
        validateFile(file);

        // Upload to MinIO
        String key = "candidatures/" + candidatId + "/lettre.pdf";
        String uploadedKey = minioService.uploadFichier(file, bucketName, key);

        // Update candidate in database
        candidat.setLettreMotivation(uploadedKey);
        candidatRepository.save(candidat);

        return DocumentUrlResponse.builder()
                .id(uploadedKey)
                .nomDocument("Lettre de motivation")
                .type("application/pdf")
                .tailleBytes(file.getSize())
                .expiresDans(urlExpirationMinutes + " minutes")
                .build();
    }

    /**
     * Get read URL for CV (only RH, ADMIN, or the owner)
     */
    public DocumentUrlResponse getUrlLectureCv(UUID candidatId, Authentication authentication) {
        Candidat candidat = getCandidatOrThrow(candidatId);

        // Check access rights
        verifyAccessRights(candidatId, authentication);

        if (candidat.getCv() == null || candidat.getCv().isEmpty()) {
            throw new DocumentNotFoundException("Ce candidat n'a pas encore de CV");
        }

        String url = minioService.genererUrlLecture(bucketName, candidat.getCv());

        return DocumentUrlResponse.builder()
                .nomDocument("CV")
                .url(url)
                .type("application/pdf")
                .expiresDans(urlExpirationMinutes + " minutes")
                .build();
    }

    /**
     * Get download URL for CV (only RH, ADMIN, or the owner)
     */
    public DocumentUrlResponse getUrlTelechargementCv(UUID candidatId, Authentication authentication) {
        Candidat candidat = getCandidatOrThrow(candidatId);

        // Check access rights
        verifyAccessRights(candidatId, authentication);

        if (candidat.getCv() == null || candidat.getCv().isEmpty()) {
            throw new DocumentNotFoundException("Ce candidat n'a pas encore de CV");
        }

        String fileName = "CV_" + candidat.getNom() + "_" + candidat.getPrenom() + ".pdf";
        String url = minioService.genererUrlTelechargement(bucketName, candidat.getCv(), fileName);

        return DocumentUrlResponse.builder()
                .nomDocument("CV")
                .url(url)
                .type("application/pdf")
                .expiresDans(urlExpirationMinutes + " minutes")
                .build();
    }

    /**
     * Get read URL for lettre de motivation (only RH, ADMIN, or the owner)
     */
    public DocumentUrlResponse getUrlLectureLettre(UUID candidatId, Authentication authentication) {
        Candidat candidat = getCandidatOrThrow(candidatId);

        // Check access rights
        verifyAccessRights(candidatId, authentication);

        if (candidat.getLettreMotivation() == null || candidat.getLettreMotivation().isEmpty()) {
            throw new DocumentNotFoundException("Ce candidat n'a pas encore de lettre de motivation");
        }

        String url = minioService.genererUrlLecture(bucketName, candidat.getLettreMotivation());

        return DocumentUrlResponse.builder()
                .nomDocument("Lettre de motivation")
                .url(url)
                .type("application/pdf")
                .expiresDans(urlExpirationMinutes + " minutes")
                .build();
    }

    /**
     * Get download URL for lettre de motivation (only RH, ADMIN, or the owner)
     */
    public DocumentUrlResponse getUrlTelechargementLettre(UUID candidatId, Authentication authentication) {
        Candidat candidat = getCandidatOrThrow(candidatId);

        // Check access rights
        verifyAccessRights(candidatId, authentication);

        if (candidat.getLettreMotivation() == null || candidat.getLettreMotivation().isEmpty()) {
            throw new DocumentNotFoundException("Ce candidat n'a pas encore de lettre de motivation");
        }

        String fileName = "Lettre_" + candidat.getNom() + "_" + candidat.getPrenom() + ".pdf";
        String url = minioService.genererUrlTelechargement(bucketName, candidat.getLettreMotivation(), fileName);

        return DocumentUrlResponse.builder()
                .nomDocument("Lettre de motivation")
                .url(url)
                .type("application/pdf")
                .expiresDans(urlExpirationMinutes + " minutes")
                .build();
    }

    // ═══════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════

    /**
     * Get candidate or throw exception if not found
     */
    private Candidat getCandidatOrThrow(UUID candidatId) {
        return candidatRepository.findById(candidatId)
                .orElseThrow(() -> new DocumentNotFoundException("Candidat non trouvé: " + candidatId));
    }

    /**
     * Validate file: check type (PDF only) and size (< 5MB)
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("Le fichier est vide");
        }

        if (file.getSize() > maxFileSize) {
            throw new InvalidFileException("Fichier trop volumineux. Taille max: 5MB");
        }

        String detectedType = tika.detect(file.getOriginalFilename());
        String contentType = file.getContentType() != null ? file.getContentType() : "";

        if (!detectedType.equals("application/pdf") && !contentType.equals("application/pdf")) {
            throw new InvalidFileException("Seuls les fichiers PDF sont acceptés");
        }
    }

    /**
     * Verify that the user has access rights:
     * - RH, ADMIN: full access
     * - CANDIDAT: only their own documents
     */
    private void verifyAccessRights(UUID candidatId, Authentication authentication) {
        if (authentication == null) {
            throw new DocumentAccessDeniedException("Authentification requise");
        }

        String userRole = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .findFirst()
                .orElse("");

        // RH, ENCADRANT, ADMIN have full access
        if (userRole.equals("ROLE_AGENT_RH") ||
                userRole.equals("ROLE_ENCADRANT") ||
                userRole.equals("ROLE_ADMIN")) {
            return;
        }

        // CANDIDAT can only access their own documents
        if (userRole.equals("ROLE_CANDIDAT")) {
            String userEmail = authentication.getName();
            Candidat candidat = getCandidatOrThrow(candidatId);

            if (!candidat.getEmail().equals(userEmail)) {
                throw new DocumentAccessDeniedException(
                        "Accès refusé. Vous pouvez uniquement accéder à vos propres documents");
            }
            return;
        }

        throw new DocumentAccessDeniedException("Rôle non autorisé");
    }
}
