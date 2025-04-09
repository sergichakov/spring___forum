
package com.forum.post.repo.mapper;

import com.forum.post.kafka.event.Post;
import com.forum.post.repo.model.PostEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

    Post entityToApi(PostEntity entity);

    PostEntity apiToEntity(Post api);
}
