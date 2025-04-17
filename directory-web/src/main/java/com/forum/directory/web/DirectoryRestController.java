package com.forum.directory.web;

import com.forum.directory.web.hateoas.model.DirectoryRest;
import com.forum.directory.web.model.DirectoryWebDto;
import com.forum.directory.web.service.DirectoryWebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ExecutionException;

@CrossOrigin
@RestController
public class DirectoryRestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryRestController.class);
    @Autowired
    private DirectoryWebService directoryWebService;

    @RequestMapping(value = "/directoriesweb", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public DeferredResult<ResponseEntity<CollectionModel<DirectoryRest>>>
    getAllDirectories(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer numberPerPage) {

        LOGGER.info("Start");
        DeferredResult<ResponseEntity<CollectionModel<DirectoryRest>>> deferredResult =
                directoryWebService.listDirectory(page, numberPerPage);
        CollectionModel<DirectoryRest> col = (CollectionModel) (deferredResult.getResult());
        Boolean hasRes = deferredResult.hasResult();
        LOGGER.info("DefferedResult Content= " + col + hasRes);
        LOGGER.info("Ending");
        return deferredResult;
    }

    @RequestMapping(value = "/directoriesweb/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<DirectoryRest>> getDirectory(@PathVariable("id") Long id) {
        LOGGER.info("Start");
        LOGGER.debug("Fetching Directory with id: {}", id);
        LOGGER.info("Thread : " + Thread.currentThread());
        DeferredResult<ResponseEntity<DirectoryRest>> deferredResult = directoryWebService.getDirectory(id);
        LOGGER.info("Ending");
        return deferredResult;
    }

    @RequestMapping(value = "/directoriesweb", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<DirectoryRest>>
    addDirectory(@RequestBody DirectoryWebDto directoryDto) throws ExecutionException, InterruptedException {

        LOGGER.info("Start");
        LOGGER.debug("Creating Directory");
        LOGGER.info("Thread : " + Thread.currentThread());
        DeferredResult<ResponseEntity<DirectoryRest>> deferredResult = directoryWebService.createDirectory(directoryDto);
        LOGGER.info("Ending");
        return deferredResult;
    }

    @RequestMapping(value = "/directoriesweb/{directoryId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<DirectoryRest>>
    updateDirectory(@PathVariable("directoryId") Long id, @RequestBody DirectoryWebDto directory) {
        LOGGER.info("Start");
        LOGGER.debug("Updating Directory with id: {}", id);
        LOGGER.info("Thread : " + Thread.currentThread());
        DeferredResult<ResponseEntity<DirectoryRest>> deferredResult = directoryWebService.updateDirectory(id, directory);
        LOGGER.info("Ending");
        return deferredResult;
    }

    @RequestMapping(value = "/directoriesweb/{directoryId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<DirectoryRest>> deleteDirectory(@PathVariable("directoryId") Long id) { // String Id
        LOGGER.info("Start");
        LOGGER.debug("Deleting Directory with id: {}", id);
        LOGGER.info("Thread : " + Thread.currentThread());
        DeferredResult<ResponseEntity<DirectoryRest>> deferredResult = directoryWebService.deleteDirectory(id);
        LOGGER.info("Ending");
        return deferredResult;
    }

    @RequestMapping(value = "/directoriesweb/maxtopic/{topicId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<Long>> getMaxTopicIdFromDirectory(){//@PathVariable("topicId") Long topicId) { // String Id
        LOGGER.info("Start");
        LOGGER.debug("Get maxTopic from directories table");
        LOGGER.info("Thread : " + Thread.currentThread());
        DeferredResult<ResponseEntity<Long>> deferredResult = directoryWebService.getMaxTopicId();//topicId);
        LOGGER.info("Ending");
        return deferredResult;
    }
}
