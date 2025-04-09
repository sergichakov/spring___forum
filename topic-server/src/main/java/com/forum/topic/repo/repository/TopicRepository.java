
package com.forum.topic.repo.repository;

import java.util.List;

import com.forum.topic.repo.model.TopicEntity;
import org.springframework.data.repository.CrudRepository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository // i added it because could not find this repository bean
@RepositoryRestResource(collectionResourceRel = "productdata", path = "productdata")
public interface TopicRepository extends CrudRepository<TopicEntity, Long> {

	public List<TopicEntity> findByPostId(@Param("postId") Long postId);
	TopicEntity findByPostIdAndUserOwnerId(@Param("postId") Long postId, @Param("userOwnerId") Long userOwnerId);
}
