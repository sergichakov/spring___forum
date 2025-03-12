package com.forum.post.repo.repository;

import com.forum.post.repo.model.PostEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.UUID;

@RepositoryRestResource(collectionResourceRel = "productdata", path = "productdata")
public interface PostPagingRepository extends PagingAndSortingRepository<PostEntity, UUID> { // было Long
    public List<PostEntity> findByPostId(@Param("topicId") UUID postId, Pageable pageable);
    //public List<PostEntity> f
}
