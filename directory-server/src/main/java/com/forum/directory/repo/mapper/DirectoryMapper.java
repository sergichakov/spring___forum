
package com.forum.directory.repo.mapper;

import com.forum.directory.repo.model.DirectoryThemeEntity;
import com.forum.directory.kafka.event.Directory;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


 //// Because could not find DirectoryMapper Bean
@Mapper(componentModel = "spring")
public interface DirectoryMapper {
    DirectoryMapper INSTANCE= Mappers.getMapper(DirectoryMapper.class);
    Directory entityToApi(DirectoryThemeEntity entity);

    DirectoryThemeEntity apiToEntity(Directory api);
}
