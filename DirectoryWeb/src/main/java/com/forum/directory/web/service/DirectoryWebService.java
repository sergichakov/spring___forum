package com.forum.directory.web.service;

import com.forum.directory.kafka.event.Directory;
import com.forum.directory.web.hateoas.model.DirectoryRest;
import com.forum.directory.web.model.DirectoryWebDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ExecutionException;

public interface DirectoryWebService {
    DeferredResult<ResponseEntity<CollectionModel<DirectoryRest>>> listDirectory(Integer page, Integer numberPerPage);
    DeferredResult<ResponseEntity<DirectoryRest>> getDirectory( Long id);
    DeferredResult<ResponseEntity<DirectoryRest>> createDirectory(DirectoryWebDto createDirectoryRest) throws ExecutionException, InterruptedException;
    DeferredResult<ResponseEntity<DirectoryRest>> updateDirectory(Long id, DirectoryWebDto directory);
    DeferredResult<ResponseEntity<DirectoryRest>> deleteDirectory(Long id);
    DeferredResult<ResponseEntity<Long>> getMaxTopicId();
}
