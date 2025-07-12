package ma.enset.ziyara.destination.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.enset.ziyara.destination.entity.DestinationType;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationSearchRequest {

    private String name;
    private Long cityId;
    private DestinationType type;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minRating;
    private List<String> tags;

    // Location-based search
    private Double latitude;
    private Double longitude;
    private Double radiusKm;

    // Pagination
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "name";
    private String sortDirection = "asc";
}