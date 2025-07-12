package ma.enset.ziyara.city.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityDto {

    private Long id;
    private String name;
    private String arabicName;
    private String region;
    private Double latitude;
    private Double longitude;
    private String description;
    private String imageUrl;
    private Boolean isPopular;
    private Integer destinationCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper method for display
    public String getDisplayName() {
        if (arabicName != null && !arabicName.trim().isEmpty()) {
            return name + " (" + arabicName + ")";
        }
        return name;
    }

    public String getCoordinates() {
        if (latitude != null && longitude != null) {
            return String.format("%.6f, %.6f", latitude, longitude);
        }
        return "N/A";
    }
}