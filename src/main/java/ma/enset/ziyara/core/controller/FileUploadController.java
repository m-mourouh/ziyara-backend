package ma.enset.ziyara.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.ziyara.core.dto.ApiResult;
import ma.enset.ziyara.core.service.FileUploadService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Management", description = "APIs for file upload and management")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/upload")
    @Operation(summary = "Upload a single file",
            description = "Upload a single image file for destinations, profiles, etc.")
    @ApiResponse(responseCode = "200", description = "File uploaded successfully")
    public ResponseEntity<ApiResult<Map<String, String>>> uploadFile(
            @Parameter(description = "Image file to upload")
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Category/folder for the file (e.g., destinations, profiles)")
            @RequestParam(defaultValue = "general") String category) {

        log.info("Uploading file: {} to category: {}", file.getOriginalFilename(), category);

        String fileUrl = fileUploadService.uploadFile(file, category);

        Map<String, String> response = Map.of(
                "fileName", file.getOriginalFilename(),
                "fileUrl", fileUrl,
                "category", category,
                "size", String.valueOf(file.getSize()),
                "contentType", file.getContentType() != null ? file.getContentType() : "unknown"
        );

        return ResponseEntity.ok(ApiResult.success(
                response,
                "File uploaded successfully"
        ));
    }

    @PostMapping("/upload/multiple")
    @Operation(summary = "Upload multiple files",
            description = "Upload multiple image files at once (max 10 files)")
    @ApiResponse(responseCode = "200", description = "Files uploaded successfully")
    public ResponseEntity<ApiResult<List<Map<String, String>>>> uploadMultipleFiles(
            @Parameter(description = "Image files to upload")
            @RequestParam("files") List<MultipartFile> files,

            @Parameter(description = "Category/folder for the files")
            @RequestParam(defaultValue = "general") String category) {

        log.info("Uploading {} files to category: {}", files.size(), category);

        List<String> fileUrls = fileUploadService.uploadFiles(files, category);

        List<Map<String, String>> response = files.stream()
                .map(file -> {
                    int index = files.indexOf(file);
                    return Map.of(
                            "fileName", file.getOriginalFilename(),
                            "fileUrl", fileUrls.get(index),
                            "category", category,
                            "size", String.valueOf(file.getSize()),
                            "contentType", file.getContentType() != null ? file.getContentType() : "unknown"
                    );
                })
                .toList();

        return ResponseEntity.ok(ApiResult.success(
                response,
                files.size() + " files uploaded successfully"
        ));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete a file",
            description = "Delete an uploaded file by its URL")
    @ApiResponse(responseCode = "200", description = "File deleted successfully")
    public ResponseEntity<ApiResult<Map<String, Object>>> deleteFile(
            @Parameter(description = "File URL to delete")
            @RequestParam String fileUrl) {

        log.info("Deleting file: {}", fileUrl);

        boolean deleted = fileUploadService.deleteFile(fileUrl);

        Map<String, Object> response = Map.of(
                "fileUrl", fileUrl,
                "deleted", deleted
        );

        String message = deleted ? "File deleted successfully" : "File not found or could not be deleted";

        return ResponseEntity.ok(ApiResult.success(response, message));
    }

    @GetMapping("/info")
    @Operation(summary = "Get file information",
            description = "Get information about an uploaded file")
    public ResponseEntity<ApiResult<Map<String, Object>>> getFileInfo(
            @Parameter(description = "File URL")
            @RequestParam String fileUrl) {

        boolean exists = fileUploadService.fileExists(fileUrl);
        long size = exists ? fileUploadService.getFileSize(fileUrl) : 0;

        Map<String, Object> response = Map.of(
                "fileUrl", fileUrl,
                "exists", exists,
                "size", size
        );

        return ResponseEntity.ok(ApiResult.success(
                response,
                exists ? "File information retrieved" : "File not found"
        ));
    }

    @GetMapping("/serve/{category}/{filename:.+}")
    @Operation(summary = "Serve uploaded file",
            description = "Serve an uploaded file for display")
    public ResponseEntity<Resource> serveFile(
            @Parameter(description = "File category")
            @PathVariable String category,

            @Parameter(description = "File name")
            @PathVariable String filename) {

        try {
            String fileUrl = "/" + category + "/" + filename;
            Path filePath = fileUploadService.getFilePath(fileUrl);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Determine content type
                String contentType = determineContentType(filename);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                log.warn("File not found or not readable: {}", fileUrl);
                return ResponseEntity.notFound().build();
            }

        } catch (MalformedURLException e) {
            log.error("Malformed URL for file: {}/{}", category, filename, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error serving file: {}/{}", category, filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/serve/**")
    @Operation(summary = "Serve any uploaded file",
            description = "Serve any uploaded file by full path")
    public ResponseEntity<Resource> serveAnyFile(
            @Parameter(description = "Full file path")
            @RequestParam String path) {

        try {
            Path filePath = fileUploadService.getFilePath(path);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String filename = filePath.getFileName().toString();
                String contentType = determineContentType(filename);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error serving file: {}", path, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Helper method to determine content type
    private String determineContentType(String filename) {
        String extension = filename.toLowerCase();

        if (extension.endsWith(".jpg") || extension.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (extension.endsWith(".png")) {
            return "image/png";
        } else if (extension.endsWith(".gif")) {
            return "image/gif";
        } else if (extension.endsWith(".webp")) {
            return "image/webp";
        } else {
            return "application/octet-stream";
        }
    }
}