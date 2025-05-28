package ru.kharevich.chatservice.utils.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.data.domain.Page;
import ru.kharevich.chatservice.dto.response.PageableResponse;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface PageMapper{

    default <T> PageableResponse<T> toResponse(Page<T> page) {
        return new PageableResponse(
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.getContent()
        );
    }

}
