package com.forum.directory.repo.mapper;

import com.forum.directory.kafka.event.Directory;
import com.forum.directory.repo.model.DirectoryThemeEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-15T20:59:55+0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Ubuntu)"
)
@Component
public class DirectoryMapperImpl implements DirectoryMapper {

    @Override
    public Directory entityToApi(DirectoryThemeEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Directory.DirectoryBuilder directory = Directory.builder();

        directory.directoryId( entity.getDirectoryId() );
        directory.order( entity.getOrder() );
        directory.subDirId( entity.getSubDirId() );
        directory.creationDate( entity.getCreationDate() );
        directory.topicId( entity.getTopicId() );
        directory.name( entity.getName() );

        return directory.build();
    }

    @Override
    public DirectoryThemeEntity apiToEntity(Directory api) {
        if ( api == null ) {
            return null;
        }

        DirectoryThemeEntity directoryThemeEntity = new DirectoryThemeEntity();

        directoryThemeEntity.setDirectoryId( api.getDirectoryId() );
        directoryThemeEntity.setOrder( api.getOrder() );
        directoryThemeEntity.setSubDirId( api.getSubDirId() );
        directoryThemeEntity.setCreationDate( api.getCreationDate() );
        directoryThemeEntity.setTopicId( api.getTopicId() );
        directoryThemeEntity.setName( api.getName() );

        return directoryThemeEntity;
    }
}
