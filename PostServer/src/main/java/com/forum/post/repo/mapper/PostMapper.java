/*
 * Copyright (c) 2024/2025 Binildas A Christudas & Apress
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forum.post.repo.mapper;

import com.forum.post.kafka.event.Post;
import com.forum.post.repo.model.PostEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


 //// Because could not find PostMapper Bean
@Mapper(componentModel = "spring")
public interface PostMapper {
    PostMapper INSTANCE= Mappers.getMapper(PostMapper.class);
    Post entityToApi(PostEntity entity);

    PostEntity apiToEntity(Post api);
}
