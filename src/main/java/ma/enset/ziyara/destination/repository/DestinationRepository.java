package ma.enset.ziyara.destination.repository;

import ma.enset.ziyara.destination.entity.Destination;
import ma.enset.ziyara.destination.entity.DestinationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long>, JpaSpecificationExecutor<Destination> {

    /**
     * Find destinations by city with pagination
     */
    Page<Destination> findByCityIdOrderByName(Long cityId, Pageable pageable);

    /**
     * Find destinations by type with pagination
     */
    Page<Destination> findByTypeOrderByName(DestinationType type, Pageable pageable);

    /**
     * Find destinations by name containing (case insensitive)
     */
    Page<Destination> findByNameContainingIgnoreCaseOrderByName(String name, Pageable pageable);

    /**
     * Find active destinations only
     */
    Page<Destination> findByActiveTrue(Pageable pageable);

    /**
     * Find popular destinations (by rating and review count)
     */
    @Query("""
        SELECT d FROM Destination d 
        WHERE d.active = true 
        ORDER BY d.averageRating DESC, d.reviewCount DESC, d.name ASC
        """)
    List<Destination> findPopularDestinations(Pageable pageable);

    /**
     * Find nearby destinations using Haversine formula
     */
    @Query(value = """
        SELECT * FROM destinations d 
        WHERE d.active = true 
        AND (6371 * acos(cos(radians(:latitude)) * cos(radians(d.latitude)) * 
             cos(radians(d.longitude) - radians(:longitude)) + 
             sin(radians(:latitude)) * sin(radians(d.latitude)))) <= :radiusKm
        ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(d.latitude)) * 
                 cos(radians(d.longitude) - radians(:longitude)) + 
                 sin(radians(:latitude)) * sin(radians(d.latitude))))
        """, nativeQuery = true)
    List<Destination> findNearbyDestinations(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm,
            Pageable pageable
    );

    /**
     * Find destinations by city name
     */
    @Query("SELECT d FROM Destination d WHERE d.city.name = :cityName AND d.active = true")
    List<Destination> findByCityName(@Param("cityName") String cityName);

    /**
     * Find destinations by tags
     */
    @Query("SELECT DISTINCT d FROM Destination d JOIN d.tags t WHERE t.name IN :tagNames AND d.active = true")
    List<Destination> findByTagNames(@Param("tagNames") List<String> tagNames);

    /**
     * Get destination statistics
     */
    @Query("SELECT COUNT(d) FROM Destination d WHERE d.active = true")
    Long countActiveDestinations();

    @Query("SELECT COUNT(d) FROM Destination d WHERE d.city.id = :cityId AND d.active = true")
    Long countByCityId(@Param("cityId") Long cityId);

    @Query("SELECT d.type, COUNT(d) FROM Destination d WHERE d.active = true GROUP BY d.type")
    List<Object[]> countByType();
}