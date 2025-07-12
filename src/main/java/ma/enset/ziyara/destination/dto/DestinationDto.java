package ma.enset.ziyara.destination.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.enset.ziyara.city.dto.CityDto;
import ma.enset.ziyara.destination.entity.DestinationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationDto {

    private Long id;
    private String name;
    private String description;
    private DestinationType type;
    private CityDto cityDto;
    private BigDecimal price;
    private Double latitude;
    private Double longitude;
    private String address;
    private String phone;
    private String website;
    private String openingHours;
    private Boolean active;
    private Double averageRating;
    private Long reviewCount;
    private List<String> imageUrls;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}