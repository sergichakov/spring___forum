package com.forum.post.web.service;

import com.forum.post.kafka.event.Post;
import com.forum.post.web.hateoas.model.PostRest;
import com.forum.post.web.model.PostWebDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface PostWebService {
    DeferredResult<ResponseEntity<PostRest>> createPost(PostWebDto createPostRest, Long headerUserId) throws ExecutionException, InterruptedException;
    DeferredResult<ResponseEntity<CollectionModel<PostRest>>> listPost(Integer page, Integer numberPerPage);
    DeferredResult<ResponseEntity<PostRest>> getPost(UUID id);
    DeferredResult<ResponseEntity<PostRest>> updatePost(UUID id, PostWebDto post, Long headerUserId) ; //(String id, Post post)
    DeferredResult<ResponseEntity<Post>> deletePost(UUID id, Long headerUserId);
}
