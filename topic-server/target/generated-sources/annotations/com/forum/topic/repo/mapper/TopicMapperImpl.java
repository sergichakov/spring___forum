package com.forum.topic.repo.mapper;

import com.forum.topic.kafka.event.Topic;
import com.forum.topic.repo.model.TopicEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-15T20:08:07+0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Ubuntu)"
)
@Component
public class TopicMapperImpl implements TopicMapper {

    @Override
    public Topic entityToApi(TopicEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Topic.TopicBuilder topic = Topic.builder();

        topic.postId( entity.getPostId() );
        topic.topicLabel( entity.getTopicLabel() );
        topic.directoryId( entity.getDirectoryId() );
        topic.postContent( entity.getPostContent() );
        topic.creationDate( entity.getCreationDate() );
        topic.changeDate( entity.getChangeDate() );
        topic.userOwnerId( entity.getUserOwnerId() );

        return topic.build();
    }

    @Override
    public TopicEntity apiToEntity(Topic api) {
        if ( api == null ) {
            return null;
        }

        TopicEntity.TopicEntityBuilder topicEntity = TopicEntity.builder();

        topicEntity.postId( api.getPostId() );
        topicEntity.topicLabel( api.getTopicLabel() );
        topicEntity.directoryId( api.getDirectoryId() );
        topicEntity.postContent( api.getPostContent() );
        topicEntity.creationDate( api.getCreationDate() );
        topicEntity.changeDate( api.getChangeDate() );
        topicEntity.userOwnerId( api.getUserOwnerId() );

        return topicEntity.build();
    }
}
