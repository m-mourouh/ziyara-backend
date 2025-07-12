package ma.enset.ziyara.destination.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.ziyara.city.entity.City;
import ma.enset.ziyara.city.repository.CityRepository;
import ma.enset.ziyara.core.dto.PageResponse;
import ma.enset.ziyara.core.exception.BadRequestException;
import ma.enset.ziyara.core.exception.ResourceNotFoundException;
import ma.enset.ziyara.destination.dto.DestinationCreateRequest;
import ma.enset.ziyara.destination.dto.DestinationDto;
import ma.enset.ziyara.destination.dto.DestinationSearchRequest;
import ma.enset.ziyara.destination.dto.DestinationUpdateRequest;
import ma.enset.ziyara.destination.entity.Destination;
import ma.enset.ziyara.destination.entity.DestinationImage;
import ma.enset.ziyara.destination.entity.DestinationTag;
import ma.enset.ziyara.destination.entity.DestinationType;
import ma.enset.ziyara.destination.mapper.DestinationMapper;
import ma.enset.ziyara.destination.repository.DestinationRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final CityRepository cityRepository;
    private final DestinationMapper destinationMapper;

    /**
     * Search destinations with advanced filtering
     */
    public PageResponse<DestinationDto> searchDestinations(DestinationSearchRequest request) {
        log.debug("Searching destinations with request: {}", request);

        Specification<Destination> spec = createSpecification(request);
        Pageable pageable = createPageable(request);

        Page<Destination> destinations = destinationRepository.findAll(spec, pageable);

        List<DestinationDto> content = destinations.getContent()
                .stream()
                .map(destinationMapper::toDto)
                .toList();

        return createPageResponse(destinations, content);
    }

    /**
     * Get all destinations with pagination
     */
    public PageResponse<DestinationDto> getAllDestinations(int page, int size, String sortBy, String sortDir) {
        log.debug("Getting all destinations - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Destination> destinations = destinationRepository.findByActiveTrue(pageable);

        List<DestinationDto> content = destinations.getContent()
                .stream()
                .map(destinationMapper::toDto)
                .toList();

        return createPageResponse(destinations, content);
    }

    /**
     * Get destination by ID
     */
    public DestinationDto getDestinationById(Long id) {
        log.debug("Getting destination by id: {}", id);

        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + id));

        return destinationMapper.toDto(destination);
    }

    /**
     * Get destinations by city
     */
    @Cacheable(value = "destinations", key = "#cityId + '_' + #page + '_' + #size")
    public PageResponse<DestinationDto> getDestinationsByCity(Long cityId, int page, int size) {
        log.debug("Getting destinations by city: {}", cityId);

        // Verify city exists
        if (!cityRepository.existsById(cityId)) {
            throw new ResourceNotFoundException("City not found with id: " + cityId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Destination> destinations = destinationRepository.findByCityIdOrderByName(cityId, pageable);

        List<DestinationDto> content = destinations.getContent()
                .stream()
                .map(destinationMapper::toDto)
                .toList();

        return createPageResponse(destinations, content);
    }

    /**
     * Get destinations by type
     */
    public PageResponse<DestinationDto> getDestinationsByType(DestinationType type, int page, int size) {
        log.debug("Getting destinations by type: {}", type);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Destination> destinations = destinationRepository.findByTypeOrderByName(type, pageable);

        List<DestinationDto> content = destinations.getContent()
                .stream()
                .map(destinationMapper::toDto)
                .toList();

        return createPageResponse(destinations, content);
    }

    /**
     * Get popular destinations (most visited/highest rated)
     */
    @Cacheable(value = "popular-destinations", key = "#limit")
    public List<DestinationDto> getPopularDestinations(int limit) {
        log.debug("Getting popular destinations, limit: {}", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Destination> destinations = destinationRepository.findPopularDestinations(pageable);

        return destinations.stream()
                .map(destinationMapper::toDto)
                .toList();
    }

    /**
     * Get nearby destinations using coordinates
     */
    public List<DestinationDto> getNearbyDestinations(Double latitude, Double longitude, Double radiusKm, int limit) {
        log.debug("Getting nearby destinations - lat: {}, lng: {}, radius: {}km", latitude, longitude, radiusKm);

        List<Destination> destinations = destinationRepository.findNearbyDestinations(
                latitude, longitude, radiusKm, PageRequest.of(0, limit)
        );

        return destinations.stream()
                .map(destinationMapper::toDto)
                .toList();
    }

    /**
     * Create new destination
     */
    @Transactional
    public DestinationDto createDestination(DestinationCreateRequest request) {
        log.debug("Creating destination: {}", request.getName());

        // Verify city exists
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + request.getCityId()));

        Destination destination = destinationMapper.toEntity(request);
        destination.setCity(city);

        // Handle tags
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<DestinationTag> tags = createTags(request.getTags(), destination);
            destination.setTags(tags);
        }

        // Handle images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            Set<DestinationImage> images = createImages(request.getImageUrls(), destination);
            destination.setImages(images);
        }

        destination = destinationRepository.save(destination);

        log.info("Created destination with id: {}", destination.getId());
        return destinationMapper.toDto(destination);
    }

    /**
     * Update destination
     */
    @Transactional
    public DestinationDto updateDestination(Long id, DestinationUpdateRequest request) {
        log.debug("Updating destination: {}", id);

        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + id));

        destinationMapper.updateEntityFromDto(request, destination);

        // Update tags if provided
        if (request.getTags() != null) {
            destination.getTags().clear();
            if (!request.getTags().isEmpty()) {
                Set<DestinationTag> tags = createTags(request.getTags(), destination);
                destination.setTags(tags);
            }
        }

        // Update images if provided
        if (request.getImageUrls() != null) {
            destination.getImages().clear();
            if (!request.getImageUrls().isEmpty()) {
                Set<DestinationImage> images = createImages(request.getImageUrls(), destination);
                destination.setImages(images);
            }
        }

        destination = destinationRepository.save(destination);

        log.info("Updated destination with id: {}", destination.getId());
        return destinationMapper.toDto(destination);
    }

    /**
     * Delete destination
     */
    @Transactional
    public void deleteDestination(Long id) {
        log.debug("Deleting destination: {}", id);

        if (!destinationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Destination not found with id: " + id);
        }

        destinationRepository.deleteById(id);
        log.info("Deleted destination with id: {}", id);
    }

    /**
     * Get destination types
     */
    public List<DestinationType> getDestinationTypes() {
        return List.of(DestinationType.values());
    }

    // Helper methods
    private Set<DestinationTag> createTags(List<String> tagNames, Destination destination) {
        Set<DestinationTag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            DestinationTag tag = DestinationTag.builder()
                    .name(tagName.trim())
                    .destination(destination)
                    .build();
            tags.add(tag);
        }
        return tags;
    }

    private Set<DestinationImage> createImages(List<String> imageUrls, Destination destination) {
        Set<DestinationImage> images = new HashSet<>();
        int order = 0;
        for (String imageUrl : imageUrls) {
            DestinationImage image = DestinationImage.builder()
                    .imageUrl(imageUrl.trim())
                    .displayOrder(order++)
                    .destination(destination)
                    .build();
            images.add(image);
        }
        return images;
    }

    private PageResponse<DestinationDto> createPageResponse(Page<Destination> destinations, List<DestinationDto> content) {
        return PageResponse.<DestinationDto>builder()
                .content(content)
                .page(destinations.getNumber())
                .size(destinations.getSize())
                .totalElements(destinations.getTotalElements())
                .totalPages(destinations.getTotalPages())
                .first(destinations.isFirst())
                .last(destinations.isLast())
                .empty(destinations.isEmpty())
                .build();
    }

    /**
     * Create dynamic specification for search
     */
    private Specification<Destination> createSpecification(DestinationSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by name (contains, case insensitive)
            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + request.getName().toLowerCase() + "%"
                ));
            }

            // Filter by city
            if (request.getCityId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("city").get("id"), request.getCityId()));
            }

            // Filter by type
            if (request.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), request.getType()));
            }

            // Filter by price range
            if (request.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), request.getMinPrice()));
            }
            if (request.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), request.getMaxPrice()));
            }

            // Filter by rating
            if (request.getMinRating() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), request.getMinRating()));
            }

            // Filter by tags
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                predicates.add(root.join("tags").get("name").in(request.getTags()));
            }

            // Filter active destinations only
            predicates.add(criteriaBuilder.equal(root.get("active"), true));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Create pageable with sorting
     */
    private Pageable createPageable(DestinationSearchRequest request) {
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "name";
        String sortDir = request.getSortDirection() != null ? request.getSortDirection() : "asc";
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        return PageRequest.of(page, size, sort);
    }
}