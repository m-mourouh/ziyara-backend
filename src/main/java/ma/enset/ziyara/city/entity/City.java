package ma.enset.ziyara.city.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ma.enset.ziyara.core.entity.BaseEntity;
import ma.enset.ziyara.destination.entity.Destination;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cities")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class City extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "arabic_name", length = 100)
    private String arabicName;

    @Column(nullable = false, length = 100)
    private String region;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(length = 1000)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_popular")
    @Builder.Default
    private Boolean isPopular = false;

    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Destination> destinations = new HashSet<>();

    // Helper method to get destination count
    public int getDestinationCount() {
        return destinations != null ? destinations.size() : 0;
    }
}