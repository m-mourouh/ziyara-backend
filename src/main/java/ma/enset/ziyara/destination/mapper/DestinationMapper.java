package ma.enset.ziyara.destination.mapper;

import ma.enset.ziyara.destination.dto.DestinationCreateRequest;
import ma.enset.ziyara.destination.dto.DestinationDto;
import ma.enset.ziyara.destination.dto.DestinationUpdateRequest;
import ma.enset.ziyara.destination.entity.Destination;
import ma.enset.ziyara.destination.entity.DestinationImage;
import ma.enset.ziyara.destination.entity.DestinationTag;
import ma.enset.ziyara.city.mapper.CityMapper;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        uses = {CityMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DestinationMapper {

    @Mapping(target = "cityDto", source = "city")
    @Mapping(target = "imageUrls", source = "images", qualifiedByName = "mapImagesToUrls")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "mapTagsToStrings")
    DestinationDto toDto(Destination destination);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "city", ignore = true) // Will be set in service layer
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "averageRating", constant = "0.0")
    @Mapping(target = "reviewCount", constant = "0L")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Destination toEntity(DestinationCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDto(DestinationUpdateRequest request, @MappingTarget Destination destination);

    List<DestinationDto> toDtoList(List<Destination> destinations);

    // Custom mapping methods
    @Named("mapImagesToUrls")
    default List<String> mapImagesToUrls(Set<DestinationImage> images) {
        if (images == null) return List.of();
        return images.stream()
                .map(DestinationImage::getImageUrl)
                .collect(Collectors.toList());
    }

    @Named("mapTagsToStrings")
    default List<String> mapTagsToStrings(Set<DestinationTag> tags) {
        if (tags == null) return List.of();
        return tags.stream()
                .map(DestinationTag::getName)
                .collect(Collectors.toList());
    }

    @Named("mapStringsToTags")
    default Set<DestinationTag> mapStringsToTags(List<String> tagNames) {
        if (tagNames == null) return Set.of();
        return tagNames.stream()
                .map(name -> DestinationTag.builder().name(name).build())
                .collect(Collectors.toSet());
    }

    @Named("mapUrlsToImages")
    default Set<DestinationImage> mapUrlsToImages(List<String> imageUrls) {
        if (imageUrls == null) return Set.of();
        return imageUrls.stream()
                .map(url -> DestinationImage.builder().imageUrl(url).build())
                .collect(Collectors.toSet());
    }
}