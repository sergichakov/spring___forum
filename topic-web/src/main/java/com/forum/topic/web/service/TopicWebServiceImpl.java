package com.forum.topic.web.service;

import com.forum.topic.kafka.event.OperationKafka;
import com.forum.kafka.request_reply_util.CompletableFutureReplyingKafkaOperations;
import com.forum.topic.kafka.event.Topic;
import com.forum.topic.kafka.event.Topics;
import com.forum.topic.kafka.event.UserDetailsRole;
import com.forum.topic.web.controller.TopicRestController;
import com.forum.topic.web.hateoas.model.TopicRest;
import com.forum.topic.web.model.TopicWebDto;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;
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
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
@Service
@NoArgsConstructor
public class TopicWebServiceImpl implements TopicWebService {
    private final Logger LOGGER = LoggerFactory.getLogger(TopicWebServiceImpl.class);
    @Autowired
    private CompletableFutureReplyingKafkaOperations<String, Topics, Topics> replyingKafkaTemplate;
    @Autowired
    private ModelMapper modelMapper;
    @Value("${kafka.topic.product.request}")
    private String requestTopic;

    @Value("${kafka.topic.product.reply}")
    private String requestReplyTopic;

    public DeferredResult<ResponseEntity<CollectionModel<TopicRest>>> listTopic(
            Integer page, Integer numberPerPage, Long headerUserId, UserDetailsRole role,Boolean allUserTopicsBool){
        DeferredResult<ResponseEntity<CollectionModel<TopicRest>>> deferredResult = new DeferredResult<>();

        Topics topicsRequest = new Topics();
        if (allUserTopicsBool){
            topicsRequest.setHeaderUserId(null);
        }else {
            topicsRequest.setHeaderUserId(headerUserId);
        }
        topicsRequest.setRole(role);
        topicsRequest.setPage(page);
        topicsRequest.setNumberPerPage(numberPerPage);
        topicsRequest.setOperation(OperationKafka.RETREIVE_ALL);
        System.out.println("topics request"+topicsRequest);


        CompletableFuture<Topics> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, topicsRequest);

        completableFuture.thenAccept(topicsFromListener -> {
            if (topicsFromListener.getOperation().equals(OperationKafka.FAILURE)) {
                deferredResult.setResult(new ResponseEntity<CollectionModel<TopicRest>>(HttpStatus.NOT_FOUND));
                return;
            }else {
                List<Topic> topicList = topicsFromListener.getTopics();

                Link links[] = {linkTo(methodOn(TopicRestController.class).getAllPosts(page, numberPerPage, null, null)).withSelfRel(),
                        linkTo(methodOn(TopicRestController.class).getAllPosts(page, numberPerPage, null, null)).withRel("getAllDirectories")};

                List<TopicRest> list = new ArrayList<TopicRest>();
                for (Topic topic : topicList) {
                    TopicRest directoryHateoas = convertEntityToHateoasEntity(topic);
                    list.add(directoryHateoas
                            .add(linkTo(methodOn(TopicRestController.class).getTopic(directoryHateoas.getPostId(), null))//directoryHateoas.getDirectoryId
                                    .withSelfRel()));

                }
                list.forEach(item -> LOGGER.debug(item.toString()));
                CollectionModel<TopicRest> result = CollectionModel.of(list, links);

                deferredResult.setResult(new ResponseEntity<CollectionModel<TopicRest>>(result, HttpStatus.OK));
            }
        }).exceptionally(ex -> {
            LOGGER.error(ex.getMessage());
            return null;
        });

        return deferredResult;
    }

    public DeferredResult<ResponseEntity<TopicRest>> getTopic(Long id, Long headerUserId, UserDetailsRole role){
        DeferredResult<ResponseEntity<TopicRest>> deferredResult = new DeferredResult<>();

        Topics topicsRequest = new Topics();
        topicsRequest.setHeaderUserId(headerUserId);
        topicsRequest.setRole(role);
        topicsRequest.setOperation(OperationKafka.RETREIVE_DETAILS);
        Topic topic = new Topic();

        topic.setPostId(id);
        List<Topic> topicRequestList = new ArrayList<>();
        topicRequestList.add(topic);
        topicsRequest.setTopics(topicRequestList);

        CompletableFuture<Topics> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, topicsRequest);

        completableFuture.thenAccept(topicsFromListener -> {
            if (topicsFromListener.getOperation().equals(OperationKafka.FAILURE)) {
                deferredResult.setResult(new ResponseEntity<TopicRest>(HttpStatus.NOT_FOUND));
                return;
            }else {
                List<Topic> topicList = topicsFromListener.getTopics();
                Topic topicRetreived = null;
                Long topicId = null;

                if (topicList.iterator().hasNext()) {
                    topicRetreived = topicList.iterator().next();
                    topicId = topicRetreived.getPostId();
                    LOGGER.debug("Topic  with topic Id : {} retreived from Backend Microservice", topicId);

                    TopicRest directoryHateoas = convertEntityToHateoasEntity(topicRetreived);
                    directoryHateoas.add(linkTo(methodOn(TopicRestController.class)
                            .getTopic(directoryHateoas.getPostId(), null)).withSelfRel());

                    deferredResult.setResult(new ResponseEntity<TopicRest>(directoryHateoas, HttpStatus.OK));

                } else {
                    LOGGER.debug("Topic  with topic Id : {} not retreived from Backend Microservice", id);
                    deferredResult.setResult(new ResponseEntity<TopicRest>(HttpStatus.NOT_FOUND));
                }
            }

        }).exceptionally(ex -> {
            LOGGER.error(ex.getMessage());
            return null;
        });
        return deferredResult;
    }

    public DeferredResult<ResponseEntity<TopicRest>> createTopic(TopicWebDto createTopicDto, Long headerUserId)
            throws ExecutionException, InterruptedException{
        DeferredResult<ResponseEntity<TopicRest>> deferredResult = new DeferredResult<>();

        Topic topicKafka =modelMapper.map(createTopicDto,  Topic.class);
        topicKafka.setCreationDate(null);
        topicKafka.setChangeDate(null);
        topicKafka.setPostId(null);
        Topics topicsRequest = new Topics();
        topicsRequest.setHeaderUserId(headerUserId);
        topicsRequest.setOperation(OperationKafka.CREATE);//Directories.CREATE
        List<Topic> topicRequestList = new ArrayList<>();
        topicRequestList.add(topicKafka);
        topicsRequest.setTopics(topicRequestList);

        CompletableFuture<Topics> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, topicsRequest);

        completableFuture.thenAccept(topicsFromListener -> {
            if (topicsFromListener.getOperation().equals(OperationKafka.FAILURE)) {
                deferredResult.setResult(new ResponseEntity<TopicRest>(HttpStatus.NOT_FOUND));
                return;
            }else {
                List<Topic> topicList = topicsFromListener.getTopics();
                Topic topicRetreived = null;
                Long postId = null;

                if (topicList.iterator().hasNext()) {
                    topicRetreived = topicList.iterator().next();
                    postId = topicRetreived.getPostId();
                    LOGGER.debug("Topic  with topic Id : {} created by Backend Microservice", postId);

                    TopicRest directoryHateoas = convertEntityToHateoasEntity(topicRetreived);
                    directoryHateoas.add(linkTo(methodOn(TopicRestController.class)
                            .getTopic(directoryHateoas.getPostId(), null)).withSelfRel());
                    deferredResult.setResult(new ResponseEntity<TopicRest>(directoryHateoas, HttpStatus.OK));

                } else {
                    LOGGER.debug("Topic  with code : {} not created by Backend Microservice");
                    deferredResult.setResult(new ResponseEntity<TopicRest>(HttpStatus.CONFLICT));
                }
            }
        }).exceptionally(ex -> {
            LOGGER.error(ex.getMessage());
            return null;
        });

        LOGGER.info("Ending");
        return deferredResult;
    }
    public DeferredResult<ResponseEntity<TopicRest>> updatePost(Long id, TopicWebDto topicDto, 
                                                                Long headerUserId, UserDetailsRole role) {
        DeferredResult<ResponseEntity<TopicRest>> deferredResult = new DeferredResult<>();
        Topic topicKafka =modelMapper.map(topicDto,  Topic.class);
        topicKafka.setCreationDate(null);
        topicKafka.setChangeDate(null);
        topicKafka.setUserOwnerId(null);
        Topics topicsRequest = new Topics();
        topicsRequest.setHeaderUserId(headerUserId);
        topicsRequest.setRole(role);
        topicsRequest.setOperation(OperationKafka.UPDATE);
        topicKafka.setPostId(id);
        List<Topic> topicRequestList = new ArrayList<>();
        topicRequestList.add(topicKafka);
        topicsRequest.setTopics(topicRequestList);

        CompletableFuture<Topics> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, topicsRequest);

        completableFuture.thenAccept(topicsFromListener -> {
            if (topicsFromListener.getOperation().equals(OperationKafka.FAILURE)) {
                deferredResult.setResult(new ResponseEntity<TopicRest>(HttpStatus.NOT_FOUND));
                return ;
            }else {
                List<Topic> topicList = topicsFromListener.getTopics();
                Topic topicRetreived = null;
                Long postId = null;

                if (topicList.iterator().hasNext()) {
                    topicRetreived = topicList.iterator().next();
                    postId = topicRetreived.getPostId();
                    LOGGER.debug("Topic  with topic Id : {} updated by Backend Microservice", postId);

                    TopicRest directoryHateoas = convertEntityToHateoasEntity(topicRetreived);
                    directoryHateoas.add(linkTo(methodOn(TopicRestController.class)
                            .getTopic(directoryHateoas.getPostId(), null)).withSelfRel());
                    deferredResult.setResult(new ResponseEntity<TopicRest>(directoryHateoas, HttpStatus.OK));

                } else {
                    LOGGER.debug("Topic  with code : {} not updated by Backend Microservice", id);
                    deferredResult.setResult(new ResponseEntity<TopicRest>(HttpStatus.NOT_FOUND));
                }
            }

        }).exceptionally(ex -> {
            LOGGER.error(ex.getMessage());
            return null;
        });
        return deferredResult;
    }
    public DeferredResult<ResponseEntity<TopicRest>> deletePost(Long id, Long headerUserId, UserDetailsRole role){
        DeferredResult<ResponseEntity<TopicRest>> deferredResult = new DeferredResult<>();

        Topics topicsRequest = new Topics();
        topicsRequest.setHeaderUserId(headerUserId);
        topicsRequest.setRole(role);
        topicsRequest.setOperation(OperationKafka.DELETE);
        List<Topic> topicRequestList = new ArrayList<>();
        Topic topicToDelete = new Topic();
        topicToDelete.setPostId(id);
        topicRequestList.add(topicToDelete);
        topicsRequest.setTopics(topicRequestList);

        CompletableFuture<Topics> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, topicsRequest);

        completableFuture.thenAccept(topicsFromListener -> {

            if (topicsFromListener.getOperation().equals(OperationKafka.SUCCESS)) {
                LOGGER.debug("Topic  with topic Id : {} deleted by Backend Microservice", id);
                deferredResult.setResult(new ResponseEntity<TopicRest>(HttpStatus.NO_CONTENT));

            }
            else {
                LOGGER.debug("Topic  with id : {} suspected not deleted by Backend Microservice", id);
                deferredResult.setResult(new ResponseEntity<TopicRest>(HttpStatus.NOT_FOUND));
            }

        }).exceptionally(ex -> {
            LOGGER.error(ex.getMessage());
            return null;
        });
        return deferredResult;
    }
        private TopicRest convertEntityToHateoasEntity(Topic topic){
        return  modelMapper.map(topic,  TopicRest.class);
    }

}
