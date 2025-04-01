
package com.forum.post.repo.repository;

import java.util.List;
import java.util.UUID;

import com.forum.post.repo.model.PostEntity;
import org.springframework.data.repository.CrudRepository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(collectionResourceRel = "productdata", path = "productdata")
public interface PostRepository extends CrudRepository<PostEntity, UUID> { // было Long
	public PostEntity findByPostIdAndUserOwnerId(@Param("postId") UUID postId, @Param("userOwnerId") Long userOwnerId);
	public List<PostEntity> findByTopicId(@Param("topicId") Long topicId);
}
