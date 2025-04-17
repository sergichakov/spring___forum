package com.forum.post.repo.mapper;

import com.forum.post.kafka.event.Post;
import com.forum.post.repo.model.PostEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-16T11:05:17+0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Ubuntu)"
)
@Component
public class PostMapperImpl implements PostMapper {

    @Override
    public Post entityToApi(PostEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Post.PostBuilder post = Post.builder();

        post.postId( entity.getPostId() );
        post.topicId( entity.getTopicId() );
        post.postContent( entity.getPostContent() );
        post.creationDate( entity.getCreationDate() );
        post.changeDate( entity.getChangeDate() );
        post.userCreatorId( entity.getUserCreatorId() );
        post.userOwnerId( entity.getUserOwnerId() );
        post.numberOfLikes( entity.getNumberOfLikes() );

        return post.build();
    }

    @Override
    public PostEntity apiToEntity(Post api) {
        if ( api == null ) {
            return null;
        }

        PostEntity postEntity = new PostEntity();

        postEntity.setPostId( api.getPostId() );
        postEntity.setTopicId( api.getTopicId() );
        postEntity.setPostContent( api.getPostContent() );
        postEntity.setCreationDate( api.getCreationDate() );
        postEntity.setChangeDate( api.getChangeDate() );
        postEntity.setUserCreatorId( api.getUserCreatorId() );
        postEntity.setUserOwnerId( api.getUserOwnerId() );
        postEntity.setNumberOfLikes( api.getNumberOfLikes() );

        return postEntity;
    }
}
