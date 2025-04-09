
package com.forum.directory.repo.mapper;

import com.forum.directory.kafka.event.Directory;
import com.forum.directory.repo.model.DirectoryThemeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface DirectoryMapper {
    DirectoryMapper INSTANCE = Mappers.getMapper(DirectoryMapper.class);

    Directory entityToApi(DirectoryThemeEntity entity);

    DirectoryThemeEntity apiToEntity(Directory api);
}
