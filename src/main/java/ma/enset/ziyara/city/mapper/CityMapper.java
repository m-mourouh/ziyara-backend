package ma.enset.ziyara.city.mapper;

import ma.enset.ziyara.city.dto.CityCreateRequest;
import ma.enset.ziyara.city.dto.CityDto;
import ma.enset.ziyara.city.dto.CityUpdateRequest;
import ma.enset.ziyara.city.entity.City;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CityMapper {

    @Mapping(target = "destinationCount", expression = "java(city.getDestinationCount())")
    CityDto toDto(City city);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "destinations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    City toEntity(CityCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "destinations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDto(CityUpdateRequest request, @MappingTarget City destination);

    List<CityDto> toDtoList(List<City> cities);
}