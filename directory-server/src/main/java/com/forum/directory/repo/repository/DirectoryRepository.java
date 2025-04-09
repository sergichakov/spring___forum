
package com.forum.directory.repo.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.forum.directory.repo.model.DirectoryThemeEntity;

@RepositoryRestResource(collectionResourceRel = "productdata", path = "productdata")
public interface DirectoryRepository extends CrudRepository<DirectoryThemeEntity, Long> {
    public Long findTopByTopicId(@Param("topicId") Long topicId);

}
