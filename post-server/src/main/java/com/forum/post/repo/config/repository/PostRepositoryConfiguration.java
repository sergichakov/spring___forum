
package com.forum.post.repo.config.repository;

import com.forum.post.repo.model.PostEntity;

import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;


@Configuration
public class PostRepositoryConfiguration implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors){
        config.exposeIdsFor(PostEntity.class);
    }
}