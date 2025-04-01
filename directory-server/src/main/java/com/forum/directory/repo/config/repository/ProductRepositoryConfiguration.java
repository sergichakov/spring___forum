
package com.forum.directory.repo.config.repository;

import com.forum.directory.repo.model.DirectoryThemeEntity;

import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;


@Configuration
public class ProductRepositoryConfiguration implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors){
        config.exposeIdsFor(DirectoryThemeEntity.class);
    }
}