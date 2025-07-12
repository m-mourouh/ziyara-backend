package ma.enset.ziyara.city.repository;

import ma.enset.ziyara.city.entity.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    /**
     * Find city by name (case insensitive)
     */
    Optional<City> findByNameIgnoreCase(String name);

    /**
     * Find cities by region
     */
    List<City> findByRegionOrderByName(String region);

    /**
     * Find popular cities
     */
    List<City> findByIsPopularTrueOrderByName();

    /**
     * Find cities by name containing (case insensitive)
     */
    Page<City> findByNameContainingIgnoreCaseOrderByName(String name, Pageable pageable);

    /**
     * Find cities by region with pagination
     */
    Page<City> findByRegionOrderByName(String region, Pageable pageable);

    /**
     * Get all cities with destination count
     */
    @Query("""
        SELECT c FROM City c 
        LEFT JOIN FETCH c.destinations 
        ORDER BY c.name
        """)
    List<City> findAllWithDestinations();

    /**
     * Find cities near coordinates
     */
    @Query(value = """
        SELECT * FROM cities c 
        WHERE (6371 * acos(cos(radians(:latitude)) * cos(radians(c.latitude)) * 
               cos(radians(c.longitude) - radians(:longitude)) + 
               sin(radians(:latitude)) * sin(radians(c.latitude)))) <= :radiusKm
        ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(c.latitude)) * 
                 cos(radians(c.longitude) - radians(:longitude)) + 
                 sin(radians(:latitude)) * sin(radians(c.latitude))))
        """, nativeQuery = true)
    List<City> findNearbyCities(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm
    );

    /**
     * Get all unique regions
     */
    @Query("SELECT DISTINCT c.region FROM City c ORDER BY c.region")
    List<String> findAllRegions();

    /**
     * Count cities by region
     */
    @Query("SELECT c.region, COUNT(c) FROM City c GROUP BY c.region")
    List<Object[]> countByRegion();
}