package ma.enset.ziyara.city.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.ziyara.city.dto.CityCreateRequest;
import ma.enset.ziyara.city.dto.CityDto;
import ma.enset.ziyara.city.dto.CityUpdateRequest;
import ma.enset.ziyara.city.entity.City;
import ma.enset.ziyara.city.mapper.CityMapper;
import ma.enset.ziyara.city.repository.CityRepository;
import ma.enset.ziyara.core.dto.PageResponse;
import ma.enset.ziyara.core.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CityService {

    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    /**
     * Get all cities with pagination
     */
    public PageResponse<CityDto> getAllCities(int page, int size, String sortBy, String sortDir) {
        log.debug("Getting all cities - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<City> cities = cityRepository.findAll(pageable);

        List<CityDto> content = cities.getContent()
                .stream()
                .map(cityMapper::toDto)
                .toList();

        return PageResponse.<CityDto>builder()
                .content(content)
                .page(cities.getNumber())
                .size(cities.getSize())
                .totalElements(cities.getTotalElements())
                .totalPages(cities.getTotalPages())
                .first(cities.isFirst())
                .last(cities.isLast())
                .empty(cities.isEmpty())
                .build();
    }

    /**
     * Get all cities as simple list
     */
    @Cacheable(value = "cities", key = "'all'")
    public List<CityDto> getAllCitiesSimple() {
        log.debug("Getting all cities as simple list");
        List<City> cities = cityRepository.findAll(Sort.by("name"));
        return cityMapper.toDtoList(cities);
    }

    /**
     * Get city by ID
     */
    public CityDto getCityById(Long id) {
        log.debug("Getting city by id: {}", id);

        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));

        return cityMapper.toDto(city);
    }

    /**
     * Get city by name
     */
    public CityDto getCityByName(String name) {
        log.debug("Getting city by name: {}", name);

        City city = cityRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with name: " + name));

        return cityMapper.toDto(city);
    }

    /**
     * Get cities by region
     */
    public List<CityDto> getCitiesByRegion(String region) {
        log.debug("Getting cities by region: {}", region);

        List<City> cities = cityRepository.findByRegionOrderByName(region);
        return cityMapper.toDtoList(cities);
    }

    /**
     * Get popular cities
     */
    @Cacheable(value = "popular-cities")
    public List<CityDto> getPopularCities() {
        log.debug("Getting popular cities");

        List<City> cities = cityRepository.findByIsPopularTrueOrderByName();
        return cityMapper.toDtoList(cities);
    }

    /**
     * Search cities by name
     */
    public PageResponse<CityDto> searchCities(String name, int page, int size) {
        log.debug("Searching cities by name: {}", name);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<City> cities = cityRepository.findByNameContainingIgnoreCaseOrderByName(name, pageable);

        List<CityDto> content = cities.getContent()
                .stream()
                .map(cityMapper::toDto)
                .toList();

        return PageResponse.<CityDto>builder()
                .content(content)
                .page(cities.getNumber())
                .size(cities.getSize())
                .totalElements(cities.getTotalElements())
                .totalPages(cities.getTotalPages())
                .first(cities.isFirst())
                .last(cities.isLast())
                .empty(cities.isEmpty())
                .build();
    }

    /**
     * Get nearby cities
     */
    public List<CityDto> getNearbyCities(Double latitude, Double longitude, Double radiusKm) {
        log.debug("Getting nearby cities - lat: {}, lng: {}, radius: {}km", latitude, longitude, radiusKm);

        List<City> cities = cityRepository.findNearbyCities(latitude, longitude, radiusKm);
        return cityMapper.toDtoList(cities);
    }

    /**
     * Get all regions
     */
    @Cacheable(value = "regions")
    public List<String> getAllRegions() {
        log.debug("Getting all regions");
        return cityRepository.findAllRegions();
    }

    /**
     * Create new city
     */
    @Transactional
    public CityDto createCity(CityCreateRequest request) {
        log.debug("Creating city: {}", request.getName());

        City city = cityMapper.toEntity(request);
        city = cityRepository.save(city);

        log.info("Created city with id: {}", city.getId());
        return cityMapper.toDto(city);
    }

    /**
     * Update city
     */
    @Transactional
    public CityDto updateCity(Long id, CityUpdateRequest request) {
        log.debug("Updating city: {}", id);

        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));

        cityMapper.updateEntityFromDto(request, city);
        city = cityRepository.save(city);

        log.info("Updated city with id: {}", city.getId());
        return cityMapper.toDto(city);
    }

    /**
     * Delete city
     */
    @Transactional
    public void deleteCity(Long id) {
        log.debug("Deleting city: {}", id);

        if (!cityRepository.existsById(id)) {
            throw new ResourceNotFoundException("City not found with id: " + id);
        }

        cityRepository.deleteById(id);
        log.info("Deleted city with id: {}", id);
    }
}