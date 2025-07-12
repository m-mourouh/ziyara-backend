package ma.enset.ziyara.city.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.ziyara.city.dto.CityCreateRequest;
import ma.enset.ziyara.city.dto.CityDto;
import ma.enset.ziyara.city.dto.CityUpdateRequest;
import ma.enset.ziyara.city.service.CityService;
import ma.enset.ziyara.core.dto.ApiResult;
import ma.enset.ziyara.core.dto.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "City Management", description = "APIs for managing Moroccan cities")
public class CityController {

    private final CityService cityService;

    @GetMapping
    @Operation(summary = "Get all cities",
            description = "Get all cities with pagination and sorting")
    public ResponseEntity<ApiResult<PageResponse<CityDto>>> getAllCities(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name") String sortBy,

            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("Getting all cities - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        PageResponse<CityDto> cities = cityService.getAllCities(page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResult.success(
                cities,
                "Retrieved " + cities.getTotalElements() + " cities"
        ));
    }

    @GetMapping("/simple")
    @Operation(summary = "Get all cities (simple list)",
            description = "Get all cities as a simple list without pagination")
    public ResponseEntity<ApiResult<List<CityDto>>> getAllCitiesSimple() {

        log.info("Getting all cities as simple list");
        List<CityDto> cities = cityService.getAllCitiesSimple();

        return ResponseEntity.ok(ApiResult.success(
                cities,
                "Retrieved " + cities.size() + " cities"
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get city by ID",
            description = "Get detailed information about a specific city")
    public ResponseEntity<ApiResult<CityDto>> getCityById(
            @Parameter(description = "City ID")
            @PathVariable Long id) {

        log.info("Getting city by ID: {}", id);
        CityDto city = cityService.getCityById(id);

        return ResponseEntity.ok(ApiResult.success(
                city,
                "City retrieved successfully"
        ));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get city by name",
            description = "Get city information by city name")
    public ResponseEntity<ApiResult<CityDto>> getCityByName(
            @Parameter(description = "City name")
            @PathVariable String name) {

        log.info("Getting city by name: {}", name);
        CityDto city = cityService.getCityByName(name);

        return ResponseEntity.ok(ApiResult.success(
                city,
                "City retrieved successfully"
        ));
    }

    @GetMapping("/region/{region}")
    @Operation(summary = "Get cities by region",
            description = "Get all cities in a specific region")
    public ResponseEntity<ApiResult<List<CityDto>>> getCitiesByRegion(
            @Parameter(description = "Region name")
            @PathVariable String region) {

        log.info("Getting cities by region: {}", region);
        List<CityDto> cities = cityService.getCitiesByRegion(region);

        return ResponseEntity.ok(ApiResult.success(
                cities,
                "Found " + cities.size() + " cities in region: " + region
        ));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular cities",
            description = "Get list of popular tourist cities")
    public ResponseEntity<ApiResult<List<CityDto>>> getPopularCities() {

        log.info("Getting popular cities");
        List<CityDto> cities = cityService.getPopularCities();

        return ResponseEntity.ok(ApiResult.success(
                cities,
                "Retrieved " + cities.size() + " popular cities"
        ));
    }

    @GetMapping("/search")
    @Operation(summary = "Search cities",
            description = "Search cities by name with pagination")
    public ResponseEntity<ApiResult<PageResponse<CityDto>>> searchCities(
            @Parameter(description = "Search term")
            @RequestParam String name,

            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Searching cities by name: {}", name);
        PageResponse<CityDto> cities = cityService.searchCities(name, page, size);

        return ResponseEntity.ok(ApiResult.success(
                cities,
                "Found " + cities.getTotalElements() + " cities matching: " + name
        ));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Get nearby cities",
            description = "Get cities near a specific location")
    public ResponseEntity<ApiResult<List<CityDto>>> getNearbyCities(
            @Parameter(description = "Latitude coordinate")
            @RequestParam @Min(-90) @Max(90) Double latitude,

            @Parameter(description = "Longitude coordinate")
            @RequestParam @Min(-180) @Max(180) Double longitude,

            @Parameter(description = "Search radius in kilometers")
            @RequestParam(defaultValue = "100") @Min(1) @Max(500) Double radiusKm) {

        log.info("Getting nearby cities - lat: {}, lng: {}, radius: {}km",
                latitude, longitude, radiusKm);

        List<CityDto> cities = cityService.getNearbyCities(latitude, longitude, radiusKm);

        return ResponseEntity.ok(ApiResult.success(
                cities,
                "Found " + cities.size() + " cities within " + radiusKm + "km"
        ));
    }

    @GetMapping("/regions")
    @Operation(summary = "Get all regions",
            description = "Get list of all regions in Morocco")
    public ResponseEntity<ApiResult<List<String>>> getAllRegions() {

        log.info("Getting all regions");
        List<String> regions = cityService.getAllRegions();

        return ResponseEntity.ok(ApiResult.success(
                regions,
                "Retrieved " + regions.size() + " regions"
        ));
    }

    @PostMapping
    @Operation(summary = "Create new city",
            description = "Create a new city")
    @ApiResponse(responseCode = "201", description = "City created successfully")
    public ResponseEntity<ApiResult<CityDto>> createCity(
            @Valid @RequestBody CityCreateRequest request) {

        log.info("Creating new city: {}", request.getName());
        CityDto city = cityService.createCity(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success(
                city,
                "City created successfully"
        ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update city",
            description = "Update an existing city")
    public ResponseEntity<ApiResult<CityDto>> updateCity(
            @Parameter(description = "City ID")
            @PathVariable Long id,

            @Valid @RequestBody CityUpdateRequest request) {

        log.info("Updating city: {}", id);
        CityDto city = cityService.updateCity(id, request);

        return ResponseEntity.ok(ApiResult.success(
                city,
                "City updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete city",
            description = "Delete a city by ID")
    @ApiResponse(responseCode = "200", description = "City deleted successfully")
    public ResponseEntity<ApiResult<Void>> deleteCity(
            @Parameter(description = "City ID")
            @PathVariable Long id) {

        log.info("Deleting city: {}", id);
        cityService.deleteCity(id);

        return ResponseEntity.ok(ApiResult.success(
                null,
                "City deleted successfully"
        ));
    }
}