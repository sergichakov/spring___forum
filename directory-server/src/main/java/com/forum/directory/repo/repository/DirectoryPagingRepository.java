package com.forum.directory.repo.repository;

import com.forum.directory.repo.model.DirectoryThemeEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "productdata", path = "productdata")
public interface DirectoryPagingRepository extends PagingAndSortingRepository<DirectoryThemeEntity, Long> {
    public List<DirectoryThemeEntity> findByName(@Param("name") String name, Pageable pageable);

}
