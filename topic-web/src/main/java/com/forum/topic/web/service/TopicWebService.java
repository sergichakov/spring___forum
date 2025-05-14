package com.forum.topic.web.service;

import com.forum.topic.kafka.event.Topic;
import com.forum.topic.kafka.event.UserDetailsRole;
import com.forum.topic.web.hateoas.model.TopicRest;
import com.forum.topic.web.model.TopicWebDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ExecutionException;

public interface TopicWebService {
    DeferredResult<ResponseEntity<TopicRest>> createTopic(TopicWebDto createPostRest, Long headerUserId) throws ExecutionException, InterruptedException;
    DeferredResult<ResponseEntity<CollectionModel<TopicRest>>> listTopic(Integer page, Integer numberPerPage, Long headerUserId, UserDetailsRole role, Boolean allUserTopics);
    DeferredResult<ResponseEntity<TopicRest>> getTopic(Long id, Long headerUserId, UserDetailsRole role);
    DeferredResult<ResponseEntity<TopicRest>> updatePost(Long id, TopicWebDto post, Long headerUserId, UserDetailsRole role) ; //(String id, Post post)
    DeferredResult<ResponseEntity<TopicRest>> deletePost(Long id, Long headerUserId, UserDetailsRole role);
}
