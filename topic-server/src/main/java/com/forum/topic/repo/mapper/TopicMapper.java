
package com.forum.topic.repo.mapper;

import com.forum.topic.kafka.event.Topic;
import com.forum.topic.repo.model.TopicEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TopicMapper {
    TopicMapper INSTANCE= Mappers.getMapper(TopicMapper.class);
    Topic entityToApi(TopicEntity entity);

    TopicEntity apiToEntity(Topic api);
}
