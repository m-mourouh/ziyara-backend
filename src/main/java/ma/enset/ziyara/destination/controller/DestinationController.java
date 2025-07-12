package ma.enset.ziyara.destination.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.ziyara.core.dto.ApiResult;
import ma.enset.ziyara.core.dto.PageResponse;
import ma.enset.ziyara.destination.dto.DestinationCreateRequest;
import ma.enset.ziyara.destination.dto.DestinationDto;
import ma.enset.ziyara.destination.dto.DestinationSearchRequest;
import ma.enset.ziyara.destination.dto.DestinationUpdateRequest;
import ma.enset.ziyara.destination.entity.DestinationType;
import ma.enset.ziyara.destination.service.DestinationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Destination Management", description = "APIs for managing tourist destinations")
public class DestinationController {

    private final DestinationService destinationService;

    @PostMapping("/search")
    @Operation(summary = "Search destinations with advanced filters",
            description = "Search destinations by name, city, type, price range, rating, and location")
    @ApiResponse(responseCode = "200", description = "Destinations found successfully")
    public ResponseEntity<ApiResult<PageResponse<DestinationDto>>> searchDestinations(
            @Valid @RequestBody DestinationSearchRequest request) {

        log.info("Searching destinations with filters: {}", request);
        PageResponse<DestinationDto> destinations = destinationService.searchDestinations(request);

        return ResponseEntity.ok(ApiResult.success(
                destinations,
                "Found " + destinations.getTotalElements() + " destinations"
        ));
    }

    @GetMapping
    @Operation(summary = "Get all destinations",
            description = "Get all destinations with pagination and sorting")
    public ResponseEntity<ApiResult<PageResponse<DestinationDto>>> getAllDestinations(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name") String sortBy,

            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("Getting all destinations - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        PageResponse<DestinationDto> destinations = destinationService
                .getAllDestinations(page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResult.success(
                destinations,
                "Retrieved " + destinations.getTotalElements() + " destinations"
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get destination by ID",
            description = "Get detailed information about a specific destination")
    public ResponseEntity<ApiResult<DestinationDto>> getDestinationById(
            @Parameter(description = "Destination ID")
            @PathVariable Long id) {

        log.info("Getting destination by ID: {}", id);
        DestinationDto destination = destinationService.getDestinationById(id);

        return ResponseEntity.ok(ApiResult.success(
                destination,
                "Destination retrieved successfully"
        ));
    }

    @GetMapping("/city/{cityId}")
    @Operation(summary = "Get destinations by city",
            description = "Get all destinations in a specific city")
    public ResponseEntity<ApiResult<PageResponse<DestinationDto>>> getDestinationsByCity(
            @Parameter(description = "City ID")
            @PathVariable Long cityId,

            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Getting destinations by city: {}", cityId);
        PageResponse<DestinationDto> destinations = destinationService
                .getDestinationsByCity(cityId, page, size);

        return ResponseEntity.ok(ApiResult.success(
                destinations,
                "Found " + destinations.getTotalElements() + " destinations in this city"
        ));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get destinations by type",
            description = "Get all destinations of a specific type")
    public ResponseEntity<ApiResult<PageResponse<DestinationDto>>> getDestinationsByType(
            @Parameter(description = "Destination type")
            @PathVariable DestinationType type,

            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Getting destinations by type: {}", type);
        PageResponse<DestinationDto> destinations = destinationService
                .getDestinationsByType(type, page, size);

        return ResponseEntity.ok(ApiResult.success(
                destinations,
                "Found " + destinations.getTotalElements() + " " + type + " destinations"
        ));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular destinations",
            description = "Get the most popular destinations based on ratings and reviews")
    public ResponseEntity<ApiResult<List<DestinationDto>>> getPopularDestinations(
            @Parameter(description = "Maximum number of destinations to return")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {

        log.info("Getting popular destinations, limit: {}", limit);
        List<DestinationDto> destinations = destinationService.getPopularDestinations(limit);

        return ResponseEntity.ok(ApiResult.success(
                destinations,
                "Retrieved " + destinations.size() + " popular destinations"
        ));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Get nearby destinations",
            description = "Get destinations near a specific location using GPS coordinates")
    public ResponseEntity<ApiResult<List<DestinationDto>>> getNearbyDestinations(
            @Parameter(description = "Latitude coordinate")
            @RequestParam @Min(-90) @Max(90) Double latitude,

            @Parameter(description = "Longitude coordinate")
            @RequestParam @Min(-180) @Max(180) Double longitude,

            @Parameter(description = "Search radius in kilometers")
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) Double radiusKm,

            @Parameter(description = "Maximum number of destinations to return")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {

        log.info("Getting nearby destinations - lat: {}, lng: {}, radius: {}km, limit: {}",
                latitude, longitude, radiusKm, limit);

        List<DestinationDto> destinations = destinationService
                .getNearbyDestinations(latitude, longitude, radiusKm, limit);

        return ResponseEntity.ok(ApiResult.success(
                destinations,
                "Found " + destinations.size() + " destinations within " + radiusKm + "km"
        ));
    }

    @PostMapping
    @Operation(summary = "Create new destination",
            description = "Create a new tourist destination")
    @ApiResponse(responseCode = "201", description = "Destination created successfully")
    public ResponseEntity<ApiResult<DestinationDto>> createDestination(
            @Valid @RequestBody DestinationCreateRequest request) {

        log.info("Creating new destination: {}", request.getName());
        DestinationDto destination = destinationService.createDestination(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success(
                destination,
                "Destination created successfully"
        ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update destination",
            description = "Update an existing destination")
    public ResponseEntity<ApiResult<DestinationDto>> updateDestination(
            @Parameter(description = "Destination ID")
            @PathVariable Long id,

            @Valid @RequestBody DestinationUpdateRequest request) {

        log.info("Updating destination: {}", id);
        DestinationDto destination = destinationService.updateDestination(id, request);

        return ResponseEntity.ok(ApiResult.success(
                destination,
                "Destination updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete destination",
            description = "Delete a destination by ID")
    @ApiResponse(responseCode = "200", description = "Destination deleted successfully")
    public ResponseEntity<ApiResult<Void>> deleteDestination(
            @Parameter(description = "Destination ID")
            @PathVariable Long id) {

        log.info("Deleting destination: {}", id);
        destinationService.deleteDestination(id);

        return ResponseEntity.ok(ApiResult.success(
                null,
                "Destination deleted successfully"
        ));
    }

    @GetMapping("/types")
    @Operation(summary = "Get all destination types",
            description = "Get list of all available destination types")
    public ResponseEntity<ApiResult<List<DestinationType>>> getDestinationTypes() {

        log.info("Getting all destination types");
        List<DestinationType> types = destinationService.getDestinationTypes();

        return ResponseEntity.ok(ApiResult.success(
                types,
                "Destination types retrieved successfully"
        ));
    }
}