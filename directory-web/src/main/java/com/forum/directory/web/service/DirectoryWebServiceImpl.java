package com.forum.directory.web.service;

import com.forum.directory.kafka.event.Directories;
import com.forum.directory.kafka.event.Directory;
import com.forum.directory.kafka.event.OperationDirectoryKafka;
import com.forum.directory.web.DirectoryRestController;
import com.forum.directory.web.hateoas.model.DirectoryRest;
import com.forum.directory.web.model.DirectoryWebDto;
import com.forum.kafka.request_reply_util.CompletableFutureReplyingKafkaOperations;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service("directoryWebServiceImpl")
@Transactional // it may not work No qualifying bean of type
public class DirectoryWebServiceImpl implements DirectoryWebService {
    private final Logger LOGGER = LoggerFactory.getLogger(DirectoryWebServiceImpl.class);
//    @Autowired
    private CompletableFutureReplyingKafkaOperations<String, Directories, Directories> replyingKafkaTemplate;
//    @Autowired
    private ModelMapper modelMapper;
    @Value("${kafka.topic.product.request}")
    private String requestTopic;

    @Value("${kafka.topic.product.reply}")
    private String requestReplyTopic;
    @Autowired
    public DirectoryWebServiceImpl(CompletableFutureReplyingKafkaOperations<String, Directories, Directories> replyingKafkaTemplate,
                                   ModelMapper modelMapper){//, String requestTopic, String requestReplyTopic) {
        this.replyingKafkaTemplate = replyingKafkaTemplate;
        this.modelMapper = modelMapper;
        //this.requestTopic = requestTopic;
        //this.requestReplyTopic = requestReplyTopic;
    }

    public DirectoryWebServiceImpl() {
    }

    public DeferredResult<ResponseEntity<CollectionModel<DirectoryRest>>> listDirectory(Integer page, Integer numberPerPage) {
        DeferredResult<ResponseEntity<CollectionModel<DirectoryRest>>> deferredResult = new DeferredResult<>();

        Directories directoriesRequest = new Directories();
        directoriesRequest.setPage(page);
        directoriesRequest.setNumberPerPage(numberPerPage);
        directoriesRequest.setOperation(OperationDirectoryKafka.RETREIVE_ALL);

        CompletableFuture<Directories> completableFuture = replyingKafkaTemplate.requestReply(requestTopic, directoriesRequest);

        completableFuture.thenAccept(directories -> {

            List<Directory> directoryList = directories.getDirectories();

            Link links[] = {linkTo(methodOn(DirectoryRestController.class).getAllDirectories(page, numberPerPage)).withSelfRel(),
                    linkTo(methodOn(DirectoryRestController.class).getAllDirectories(page, numberPerPage)).withRel("getAllDirectories")};

            List<DirectoryRest> list = new ArrayList<DirectoryRest>();
            for (Directory directory : directoryList) {
                DirectoryRest directoryHateoas = convertEntityToHateoasEntity(directory);
                list.add(directoryHateoas
                        .add(linkTo(methodOn(DirectoryRestController.class).getDirectory(directoryHateoas.getDirectoryId()))
                                .withSelfRel()));

            }
            list.forEach(item -> LOGGER.debug(item.toString()));
            CollectionModel<DirectoryRest> result = CollectionModel.of(list, links);

            deferredResult.setResult(new ResponseEntity<CollectionModel<DirectoryRest>>(result, HttpStatus.OK));

        }).exceptionally(ex -> {
            LOGGER.error(ex.getMessage());
            return null;
        });

        //delay();
        return deferredResult;
    }

    public DeferredResult<ResponseEntity<DirectoryRest>> getDirectory(Long id) {
        DeferredResult<ResponseEntity<DirectoryRest>> deferredResult = new DeferredResult<>();

        Directories directoriesRequest = new Directories();
        directoriesRequest.setOperation(OperationDirectoryKafka.RETREIVE_DETAILS);
        Directory directory = new Directory();
        directory.setDirectoryId(id);
        List<Directory> directoryRequestList = new ArrayList<>();
        directoryRequestList.add(directory);
        directoriesRequest.setDirectories(directoryRequestList);

        CompletableFuture<Directories> completableFuture = replyingKafkaTemplate.requestReply(requestTopic, directoriesRequest);

        completableFuture.thenAccept(directories -> {

            List<Directory> directoryList = directories.getDirectories();
            Directory directoryRetreived = null;
            Long directoryId = null; // String directoryId

            if (directoryList.iterator().hasNext()) {
                directoryRetreived = directoryList.iterator().next();
                directoryId = directoryRetreived.getDirectoryId();
                LOGGER.debug("Directory with directoryId : {} retreived from Backend Microservice", directoryId);

                DirectoryRest directoryHateoas = convertEntityToHateoasEntity(directoryRetreived);
                directoryHateoas.add(linkTo(methodOn(DirectoryRestController.class).getDirectory(directoryHateoas.getDirectoryId())).withSelfRel());

                deferredResult.setResult(new ResponseEntity<DirectoryRest>(directoryHateoas, HttpStatus.OK));

            } else {
                LOGGER.debug("Directory with directoryId : {} not retreived from Backend Microservice", id);
                deferredResult.setResult(new ResponseEntity<DirectoryRest>(HttpStatus.NOT_FOUND));
            }

        }).exceptionally(ex -> {
            LOGGER.error(ex.getMessage());
            return null;
        });
        return deferredResult;
    }

    public DeferredResult<ResponseEntity<DirectoryRest>> createDirectory(DirectoryWebDto createDirectoryWebDto) throws ExecutionException, InterruptedException {
        DeferredResult<ResponseEntity<DirectoryRest>> deferredResult = new DeferredResult<>();

        Directory directoryKafka = modelMapper.map(createDirectoryWebDto, Directory.class);
        directoryKafka.setDirectoryId(null);
        directoryKafka.setCreationDate(null);    //// it was added
        Directories directoriesRequest = new Directories();
        directoriesRequest.setOperation(OperationDirectoryKafka.CREATE);
        List<Directory> directoryRequestList = new ArrayList<>();
        directoryRequestList.add(directoryKafka);
        directoriesRequest.setDirectories(directoryRequestList);

        CompletableFuture<Directories> completableFuture = replyingKafkaTemplate.requestReply(requestTopic, directoriesRequest);

        completableFuture.thenAccept(directories -> {

            List<Directory> directoryList = directories.getDirectories();
            Directory directoryRetreived = null;
            Long directoryId = null;

            if (directoryList.iterator().hasNext()) {
                directoryRetreived = directoryList.iterator().next();
                directoryId = directoryRetreived.getDirectoryId();
                LOGGER.debug("Directory with directoryId : {} created by Backend Microservice", directoryId);

                DirectoryRest directoryHateoas = convertEntityToHateoasEntity(directoryRetreived);
                directoryHateoas.add(linkTo(methodOn(DirectoryRestController.class).getDirectory(directoryHateoas.getDirectoryId())).withSelfRel());
                deferredResult.setResult(new ResponseEntity<DirectoryRest>(directoryHateoas, HttpStatus.OK));

            } else {
                LOGGER.debug("Directory with code : {} not created by Backend Microservice");
                deferredResult.setResult(new ResponseEntity<DirectoryRest>(HttpStatus.CONFLICT));
            }

        }).exceptionally(ex -> {
            LOGGER.error(ex.getMessage());
            return null;
        });

        LOGGER.info("Ending");
        return deferredResult;
    }

    public DeferredResult<ResponseEntity<DirectoryRest>> updateDirectory(Long id, DirectoryWebDto directoryWebDto) {
        DeferredResult<ResponseEntity<DirectoryRest>> deferredResult = new DeferredResult<>();
        Directory directory = modelMapper.map(directoryWebDto, Directory.class);
        directory.setDirectoryId(id);
        directory.setCreationDate(null);                //// it was added
        Directories directoriesRequest = new Directories();
        directoriesRequest.setOperation(OperationDirectoryKafka.UPDATE);
        List<Directory> directoryRequestList = new ArrayList<>();
        directoryRequestList.add(directory);
        directoriesRequest.setDirectories(directoryRequestList);

        CompletableFuture<Directories> completableFuture = replyingKafkaTemplate.requestReply(requestTopic, directoriesRequest);

        completableFuture.thenAccept(directories -> {

            List<Directory> directoryList = directories.getDirectories();
            Directory directoryRetreived = null;
            Long directoryId = null;

            if (directoryList.iterator().hasNext()) {
                directoryRetreived = directoryList.iterator().next();
                directoryId = directoryRetreived.getDirectoryId();
                LOGGER.debug("Directory with directoryId : {} updated by Backend Microservice", directoryId);

                DirectoryRest directoryHateoas = convertEntityToHateoasEntity(directoryRetreived);
                directoryHateoas.add(linkTo(methodOn(DirectoryRestController.class).getDirectory(directoryHateoas.getDirectoryId())).withSelfRel());
                deferredResult.setResult(new ResponseEntity<DirectoryRest>(directoryHateoas, HttpStatus.OK));

            } else {
                LOGGER.debug("Directory  with code : {} not updated by Backend Microservice", directoryId);
                deferredResult.setResult(new ResponseEntity<DirectoryRest>(HttpStatus.NOT_FOUND));
            }


        }).exceptionally(ex -> {
            LOGGER.error(ex.getMessage());
            return null;
        });
        return deferredResult;
    }

    public DeferredResult<ResponseEntity<DirectoryRest>> deleteDirectory(Long id) {
        DeferredResult<ResponseEntity<DirectoryRest>> deferredResult = new DeferredResult<>();

        Directories directoriesRequest = new Directories();
        directoriesRequest.setOperation(OperationDirectoryKafka.DELETE);

        List<Directory> directoryRequestList = new ArrayList<>();
        Directory directoryToDelete = new Directory();
        directoryToDelete.setDirectoryId(id);

        directoryRequestList.add(directoryToDelete);
        directoriesRequest.setDirectories(directoryRequestList);

        CompletableFuture<Directories> completableFuture = replyingKafkaTemplate.requestReply(requestTopic, directoriesRequest);

        completableFuture.thenAccept(directories -> {

            if (directories.getOperation().equals(OperationDirectoryKafka.SUCCESS)) {
                LOGGER.debug("Directory with directoryId : {} deleted by Backend Microservice", id);
                deferredResult.setResult(new ResponseEntity<DirectoryRest>(HttpStatus.NO_CONTENT));

            } else {
                LOGGER.debug("Directory with id : {} suspected not deleted by Backend Microservice", id);
                deferredResult.setResult(new ResponseEntity<DirectoryRest>(HttpStatus.NOT_FOUND));
            }

        }).exceptionally(ex -> {
            LOGGER.error(ex.getMessage());
            return null;
        });
        return deferredResult;
    }

    private DirectoryRest convertEntityToHateoasEntity(Directory directory) {
        return modelMapper.map(directory, DirectoryRest.class);
    }

    public DeferredResult<ResponseEntity<Long>> getMaxTopicId(){//Long topicId) {
        DeferredResult<ResponseEntity<Long>> deferredResult = new DeferredResult<>();

        Directories directoriesRequest = new Directories();
        directoriesRequest.setOperation(OperationDirectoryKafka.GET_MAX_TOPIC_ID);//Directories.DELETE
        //Directory directory=new Directory();
        //directory.setTopicId(topicId);
        //List<Directory> directoryRequestList = new ArrayList<>();
        //directoryRequestList.add(directory);
        //directoriesRequest.setDirectories(directoryRequestList);

        CompletableFuture<Directories> completableFuture = replyingKafkaTemplate.requestReply(requestTopic, directoriesRequest);

        completableFuture.thenAccept(maxTopicId -> {

            if (maxTopicId.getOperation().equals(OperationDirectoryKafka.SUCCESS)) {//contentEquals
                LOGGER.debug("Directory with directoryId : {} deleted by Backend Microservice");
                ResponseEntity<Long> maxId = new ResponseEntity<>(maxTopicId.getMax(), HttpStatus.OK);

                deferredResult.setResult(maxId);

            } else {
                LOGGER.debug("Directory with id : {} suspected not deleted by Backend Microservice");
                deferredResult.setResult(new ResponseEntity<Long>(HttpStatus.NOT_FOUND));
            }

        }).exceptionally(ex -> {
            LOGGER.error(ex.getMessage());
            return null;
        });
        return deferredResult;
    }
}
