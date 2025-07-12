package ma.enset.ziyara.destination.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.ziyara.core.dto.ApiResult;
import ma.enset.ziyara.core.exception.ResourceNotFoundException;
import ma.enset.ziyara.core.service.FileUploadService;
import ma.enset.ziyara.destination.entity.Destination;
import ma.enset.ziyara.destination.entity.DestinationImage;
import ma.enset.ziyara.destination.repository.DestinationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/destinations/{destinationId}/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Destination Images", description = "APIs for managing destination images")
public class DestinationImageController {

    private final FileUploadService fileUploadService;
    private final DestinationRepository destinationRepository;

    @PostMapping("/upload")
    @Operation(summary = "Upload images for a destination",
            description = "Upload one or more images for a specific destination")
    @Transactional
    public ResponseEntity<ApiResult<List<Map<String, String>>>> uploadDestinationImages(
            @Parameter(description = "Destination ID")
            @PathVariable Long destinationId,

            @Parameter(description = "Image files to upload")
            @RequestParam("files") List<MultipartFile> files) {

        log.info("Uploading {} images for destination: {}", files.size(), destinationId);

        // Verify destination exists
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + destinationId));

        // Upload files to destinations category
        List<String> fileUrls = fileUploadService.uploadFiles(files, "destinations");

        // Create DestinationImage entities
        int currentMaxOrder = destination.getImages().stream()
                .mapToInt(DestinationImage::getDisplayOrder)
                .max().orElse(-1);

        for (int i = 0; i < fileUrls.size(); i++) {
            DestinationImage image = DestinationImage.builder()
                    .imageUrl(fileUrls.get(i))
                    .displayOrder(currentMaxOrder + i + 1)
                    .destination(destination)
                    .build();

            destination.getImages().add(image);
        }

        destinationRepository.save(destination);

        // Prepare response
        List<Map<String, String>> response = files.stream()
                .map(file -> {
                    int index = files.indexOf(file);
                    return Map.of(
                            "fileName", file.getOriginalFilename(),
                            "fileUrl", fileUrls.get(index),
                            "displayOrder", String.valueOf(currentMaxOrder + index + 1)
                    );
                })
                .toList();

        return ResponseEntity.ok(ApiResult.success(
                response,
                files.size() + " images uploaded successfully"
        ));
    }

    @PostMapping("/upload/single")
    @Operation(summary = "Upload single image for destination",
            description = "Upload a single image for a specific destination")
    @Transactional
    public ResponseEntity<ApiResult<Map<String, String>>> uploadSingleDestinationImage(
            @Parameter(description = "Destination ID")
            @PathVariable Long destinationId,

            @Parameter(description = "Image file to upload")
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Image caption (optional)")
            @RequestParam(required = false) String caption) {

        log.info("Uploading single image for destination: {}", destinationId);

        // Verify destination exists
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + destinationId));

        // Upload file
        String fileUrl = fileUploadService.uploadFile(file, "destinations");

        // Create DestinationImage entity
        int displayOrder = destination.getImages().stream()
                .mapToInt(DestinationImage::getDisplayOrder)
                .max().orElse(-1) + 1;

        DestinationImage image = DestinationImage.builder()
                .imageUrl(fileUrl)
                .caption(caption)
                .displayOrder(displayOrder)
                .destination(destination)
                .build();

        destination.getImages().add(image);
        destinationRepository.save(destination);

        Map<String, String> response = Map.of(
                "fileName", file.getOriginalFilename(),
                "fileUrl", fileUrl,
                "caption", caption != null ? caption : "",
                "displayOrder", String.valueOf(displayOrder)
        );

        return ResponseEntity.ok(ApiResult.success(
                response,
                "Image uploaded successfully"
        ));
    }

    @DeleteMapping("/{imageId}")
    @Operation(summary = "Delete destination image",
            description = "Delete a specific image from a destination")
    @Transactional
    public ResponseEntity<ApiResult<Map<String, Object>>> deleteDestinationImage(
            @Parameter(description = "Destination ID")
            @PathVariable Long destinationId,

            @Parameter(description = "Image ID")
            @PathVariable Long imageId) {

        log.info("Deleting image {} from destination: {}", imageId, destinationId);

        // Verify destination exists
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + destinationId));

        // Find and remove the image
        DestinationImage imageToDelete = destination.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));

        // Delete file from storage
        boolean fileDeleted = fileUploadService.deleteFile(imageToDelete.getImageUrl());

        // Remove from destination
        destination.getImages().remove(imageToDelete);
        destinationRepository.save(destination);

        Map<String, Object> response = Map.of(
                "imageId", imageId,
                "fileUrl", imageToDelete.getImageUrl(),
                "fileDeleted", fileDeleted,
                "removedFromDestination", true
        );

        return ResponseEntity.ok(ApiResult.success(
                response,
                "Image deleted successfully"
        ));
    }

    @PutMapping("/{imageId}/reorder")
    @Operation(summary = "Reorder destination image",
            description = "Change the display order of a destination image")
    @Transactional
    public ResponseEntity<ApiResult<Map<String, Object>>> reorderDestinationImage(
            @Parameter(description = "Destination ID")
            @PathVariable Long destinationId,

            @Parameter(description = "Image ID")
            @PathVariable Long imageId,

            @Parameter(description = "New display order")
            @RequestParam Integer newOrder) {

        log.info("Reordering image {} in destination {} to order: {}", imageId, destinationId, newOrder);

        // Verify destination exists
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + destinationId));

        // Find the image
        DestinationImage imageToReorder = destination.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));

        // Update display order
        imageToReorder.setDisplayOrder(newOrder);
        destinationRepository.save(destination);

        Map<String, Object> response = Map.of(
                "imageId", imageId,
                "oldOrder", imageToReorder.getDisplayOrder(),
                "newOrder", newOrder
        );

        return ResponseEntity.ok(ApiResult.success(
                response,
                "Image order updated successfully"
        ));
    }

    @PutMapping("/{imageId}/caption")
    @Operation(summary = "Update image caption",
            description = "Update the caption of a destination image")
    @Transactional
    public ResponseEntity<ApiResult<Map<String, String>>> updateImageCaption(
            @Parameter(description = "Destination ID")
            @PathVariable Long destinationId,

            @Parameter(description = "Image ID")
            @PathVariable Long imageId,

            @Parameter(description = "New caption")
            @RequestParam String caption) {

        log.info("Updating caption for image {} in destination {}", imageId, destinationId);

        // Verify destination exists
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + destinationId));

        // Find the image
        DestinationImage imageToUpdate = destination.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));

        // Update caption
        String oldCaption = imageToUpdate.getCaption();
        imageToUpdate.setCaption(caption);
        destinationRepository.save(destination);

        Map<String, String> response = Map.of(
                "imageId", String.valueOf(imageId),
                "oldCaption", oldCaption != null ? oldCaption : "",
                "newCaption", caption
        );

        return ResponseEntity.ok(ApiResult.success(
                response,
                "Image caption updated successfully"
        ));
    }

    @GetMapping
    @Operation(summary = "Get all destination images",
            description = "Get all images for a specific destination")
    public ResponseEntity<ApiResult<List<Map<String, Object>>>> getDestinationImages(
            @Parameter(description = "Destination ID")
            @PathVariable Long destinationId) {

        log.info("Getting images for destination: {}", destinationId);

        // Verify destination exists
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + destinationId));

        // Get images sorted by display order
        List<Map<String, Object>> images = destination.getImages().stream()
                .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                .map(img -> Map.of(
                        "id", (Object) img.getId(),
                        "imageUrl", (Object) img.getImageUrl(),
                        "caption", (Object) (img.getCaption() != null ? img.getCaption() : ""),
                        "displayOrder", (Object) img.getDisplayOrder(),
                        "createdAt", (Object) img.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(ApiResult.success(
                images,
                "Retrieved " + images.size() + " images"
        ));
    }
}