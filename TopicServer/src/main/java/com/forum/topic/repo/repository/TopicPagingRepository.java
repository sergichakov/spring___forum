package com.forum.topic.repo.repository;

import com.forum.topic.repo.model.TopicEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
//@Repository("entityManagerFactory")// i added it because could not find this repository bean. I DISABLED IT becouse conflic in JpaRepositoryFactoryBean
@RepositoryRestResource(collectionResourceRel = "productdata", path = "productdata")
public interface TopicPagingRepository extends PagingAndSortingRepository<TopicEntity, Long> { // было Long
    public List<TopicEntity> findByPostId(@Param("postId") Long postId, Pageable pageable);
    //public List<PostEntity> f
    public List<TopicEntity> findByPostIdAndUserOwnerId(@Param("postId") Long postId, @Param("userOwnerId") Long userOwnerId, Pageable pageable);
    public List<TopicEntity> findByUserOwnerId(@Param("userOwnerId") Long userOwnerId, Pageable pageable);
}
