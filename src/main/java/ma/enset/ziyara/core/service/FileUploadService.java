package ma.enset.ziyara.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.ziyara.core.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    @Value("${app.file-upload.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.file-upload.max-size:10485760}") // 10MB
    private long maxFileSize;

    @Value("${app.file-upload.allowed-extensions:jpg,jpeg,png,gif,webp}")
    private String allowedExtensions;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    /**
     * Upload a single file
     */
    public String uploadFile(MultipartFile file, String category) {
        log.debug("Uploading file: {} to category: {}", file.getOriginalFilename(), category);

        validateFile(file);

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = createUploadDirectory(category);

            // Generate unique filename
            String fileName = generateUniqueFileName(file);

            // Save file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative URL
            String fileUrl = "/" + category + "/" + fileName;
            log.info("File uploaded successfully: {}", fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Upload multiple files
     */
    public List<String> uploadFiles(List<MultipartFile> files, String category) {
        log.debug("Uploading {} files to category: {}", files.size(), category);

        if (files.size() > 10) {
            throw new BadRequestException("Cannot upload more than 10 files at once");
        }

        return files.stream()
                .map(file -> uploadFile(file, category))
                .toList();
    }

    /**
     * Delete a file
     */
    public boolean deleteFile(String fileUrl) {
        log.debug("Deleting file: {}", fileUrl);

        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return false;
            }

            // Remove leading slash and construct path
            String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path filePath = Paths.get(uploadDir, relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", fileUrl);
                return true;
            } else {
                log.warn("File not found for deletion: {}", fileUrl);
                return false;
            }

        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
            return false;
        }
    }

    /**
     * Get file path for serving
     */
    public Path getFilePath(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new BadRequestException("File URL cannot be empty");
        }

        // Remove leading slash and construct path
        String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
        Path filePath = Paths.get(uploadDir, relativePath);

        if (!Files.exists(filePath)) {
            throw new BadRequestException("File not found: " + fileUrl);
        }

        return filePath;
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String fileUrl) {
        try {
            Path filePath = getFilePath(fileUrl);
            return Files.exists(filePath);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get file size
     */
    public long getFileSize(String fileUrl) {
        try {
            Path filePath = getFilePath(fileUrl);
            return Files.size(filePath);
        } catch (Exception e) {
            return 0;
        }
    }

    // Private helper methods

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new BadRequestException(
                    String.format("File size exceeds maximum allowed size of %d bytes", maxFileSize)
            );
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException(
                    "Invalid file type. Allowed types: " + String.join(", ", ALLOWED_CONTENT_TYPES)
            );
        }

        // Check file extension
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = getFileExtension(originalFilename);

        List<String> allowedExts = Arrays.asList(allowedExtensions.toLowerCase().split(","));
        if (!allowedExts.contains(extension.toLowerCase())) {
            throw new BadRequestException(
                    "Invalid file extension. Allowed extensions: " + allowedExtensions
            );
        }

        // Check for path traversal
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Invalid file name");
        }
    }

    private Path createUploadDirectory(String category) throws IOException {
        Path uploadPath = Paths.get(uploadDir, category);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
        }

        return uploadPath;
    }

    private String generateUniqueFileName(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = getFileExtension(originalFilename);
        String baseName = getBaseName(originalFilename);

        // Create timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        // Generate unique ID
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        // Combine: timestamp_uniqueId_originalName.extension
        return String.format("%s_%s_%s.%s", timestamp, uniqueId,
                sanitizeFileName(baseName), extension);
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    private String getBaseName(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(0, lastDotIndex) : filename;
    }

    private String sanitizeFileName(String filename) {
        // Remove or replace invalid characters
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_{2,}", "_") // Replace multiple underscores with single
                .substring(0, Math.min(filename.length(), 50)); // Limit length
    }
}