package ru.kharevich.userservice.util.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.dto.response.UserResponse;
import ru.kharevich.userservice.model.User;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "accountStatus", expression = "java(ru.kharevich.userservice.model.AccountStatus.MODIFYING)")
    User toEntity(UserRequest userRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserByRequest(UserRequest request, @MappingTarget User user);

}
