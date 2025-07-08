package ma.enset.ziyara.auth.mapper;

import ma.enset.ziyara.auth.dto.RegisterRequest;
import ma.enset.ziyara.auth.dto.UserDto;
import ma.enset.ziyara.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "accountNonExpired", constant = "true")
    @Mapping(target = "accountNonLocked", constant = "true")
    @Mapping(target = "credentialsNonExpired", constant = "true")
    User toEntity(RegisterRequest registerRequest);

    @Mapping(target = "password", ignore = true)
    void updateUserFromDto(UserDto userDto, @MappingTarget User user);
}