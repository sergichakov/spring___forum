package com.forum.post.web.service;

import com.forum.post.kafka.event.Post;
import com.forum.post.kafka.event.Posts;
import com.forum.post.kafka.event.OperationKafka;
import com.forum.kafka.request_reply_util.CompletableFutureReplyingKafkaOperations;
import com.forum.post.web.controller.PostRestController;
import com.forum.post.web.hateoas.model.PostRest;
import com.forum.post.web.model.PostWebDto;
import lombok.NoArgsConstructor;
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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
@Service
//@RequiredArgsConstructor
@NoArgsConstructor
public class PostWebServiceImpl implements PostWebService {
    private final Logger LOGGER = LoggerFactory.getLogger(PostWebServiceImpl.class);
    @Autowired
    private CompletableFutureReplyingKafkaOperations<String, Posts, Posts> replyingKafkaTemplate;
    @Autowired
    private ModelMapper modelMapper;
    @Value("${kafka.topic.product.request}")
    private String requestTopic;

    @Value("${kafka.topic.product.reply}")
    private String requestReplyTopic;

    public DeferredResult<ResponseEntity<CollectionModel<PostRest>>> listPost(Integer page, Integer numberPerPage){
        DeferredResult<ResponseEntity<CollectionModel<PostRest>>> deferredResult = new DeferredResult<>();

        Posts postsRequest = new Posts();
        postsRequest.setPage(page);
        postsRequest.setNumberPerPage(numberPerPage);
        postsRequest.setOperation(OperationKafka.RETREIVE_ALL);  //Directories.RETREIVE_ALL)

        CompletableFuture<Posts> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, postsRequest);

        completableFuture.thenAccept(directories -> {

            List<Post> postList = directories.getPosts();

            Link links[] = { linkTo(methodOn(PostRestController.class).getAllPosts(page,numberPerPage)).withSelfRel(),
                    linkTo(methodOn(PostRestController.class).getAllPosts(page,numberPerPage)).withRel("getAllDirectories") };

            List<PostRest> list = new ArrayList<PostRest>();
            for (Post post : postList) {
                PostRest directoryHateoas = convertEntityToHateoasEntity(post);
                list.add(directoryHateoas
                        .add(linkTo(methodOn(PostRestController.class).getPost(directoryHateoas.getPostId()))//directoryHateoas.getDirectoryId
                                .withSelfRel()));

            }
            list.forEach(item -> LOGGER.debug(item.toString()));
            CollectionModel<PostRest> result = CollectionModel.of(list, links);

            deferredResult.setResult(new ResponseEntity<CollectionModel<PostRest>>(result, HttpStatus.OK));

        }).exceptionally(ex -> {
            LOGGER.error(ex.getMessage());
            return null;
        });

        //delay();
        return deferredResult;
    }
    public DeferredResult<ResponseEntity<PostRest>> getPost(UUID id){
        DeferredResult<ResponseEntity<PostRest>> deferredResult = new DeferredResult<>();

        Posts postsRequest = new Posts();
        postsRequest.setOperation(OperationKafka.RETREIVE_DETAILS);//Directories.RETREIVE_DETAILS
        Post post = new Post();
        post.setPostId(id);//// почему так я не понял Нафига
        List<Post> postRequestList = new ArrayList<>();
        postRequestList.add(post);
        postsRequest.setPosts(postRequestList);

        CompletableFuture<Posts> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, postsRequest);

        completableFuture.thenAccept(directories -> {

            List<Post> postList = directories.getPosts();
            Post postRetreived = null;
            UUID postId = null; // String directoryId

            if (postList.iterator().hasNext()) {
                postRetreived = postList.iterator().next();
                postId = postRetreived.getPostId();//postRetreived.getDirectoryId()
                LOGGER.debug("Product with productId : {} retreived from Backend Microservice", postId);

                PostRest directoryHateoas = convertEntityToHateoasEntity(postRetreived);
                directoryHateoas.add(linkTo(methodOn(PostRestController.class).getPost(directoryHateoas.getPostId())).withSelfRel()); //directoryHateoas.getDirectoryId())).withSelfRel

                deferredResult.setResult(new ResponseEntity<PostRest>(directoryHateoas, HttpStatus.OK));

            }
            else {
                LOGGER.debug("Product with productId : {} not retreived from Backend Microservice", id);
                deferredResult.setResult(new ResponseEntity<PostRest>(HttpStatus.NOT_FOUND));
            }

        }).exceptionally(ex -> {
            LOGGER.error(ex.getMessage());
            return null;
        });
        return deferredResult;
    }
    public DeferredResult<ResponseEntity<PostRest>> createPost(PostWebDto createPostRest, Long headerUserId) throws ExecutionException, InterruptedException{
        DeferredResult<ResponseEntity<PostRest>> deferredResult = new DeferredResult<>();

        Post postRest =modelMapper.map(createPostRest,  Post.class); // очень интересно, сработает ли
        postRest.setPostId(null);
        Posts postsRequest = new Posts();
        postsRequest.setOperation(OperationKafka.CREATE);//Directories.CREATE
        List<Post> postRequestList = new ArrayList<>();
        postRequestList.add(postRest);
        postsRequest.setPosts(postRequestList);
        postsRequest.setHeaderUserId(headerUserId);

        CompletableFuture<Posts> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, postsRequest);

        completableFuture.thenAccept(directories -> {

            List<Post> postList = directories.getPosts();
            Post postRetreived = null;
            UUID directoryId = null; //String directoryId

            if (postList.iterator().hasNext()) {
                postRetreived = postList.iterator().next();
                directoryId = postRetreived.getPostId(); //postRetreived.getDirectoryId();
                LOGGER.debug("Product with productId : {} created by Backend Microservice", directoryId);

                PostRest directoryHateoas = convertEntityToHateoasEntity(postRetreived);
                directoryHateoas.add(linkTo(methodOn(PostRestController.class).getPost(directoryHateoas.getPostId())).withSelfRel()); //directoryHateoas.getDirectoryId())).withSelfRel
                deferredResult.setResult(new ResponseEntity<PostRest>(directoryHateoas, HttpStatus.OK));

            }
            else {
                LOGGER.debug("Product with code : {} not created by Backend Microservice");//, directory.getCode());
                deferredResult.setResult(new ResponseEntity<PostRest>(HttpStatus.CONFLICT));
            }

        }).exceptionally(ex -> {
            LOGGER.error(ex.getMessage());
            return null;
        });

        LOGGER.info("Ending");
        return deferredResult;
    }
    public DeferredResult<ResponseEntity<PostRest>> updatePost(UUID id, PostWebDto postDto, Long headerUserId) {
        DeferredResult<ResponseEntity<PostRest>> deferredResult = new DeferredResult<>();
        Post post =modelMapper.map(postDto,  Post.class);
        Posts postsRequest = new Posts();
        postsRequest.setHeaderUserId(headerUserId);
        postsRequest.setOperation(OperationKafka.UPDATE);//Directories.UPDATE
        post.setPostId(id); // but may be i need different way - give Post object as it is
        List<Post> postRequestList = new ArrayList<>();
        postRequestList.add(post);
        postsRequest.setPosts(postRequestList);

        CompletableFuture<Posts> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, postsRequest);

        completableFuture.thenAccept(directories -> {

            List<Post> postList = directories.getPosts();
            Post postRetreived = null;
            UUID directoryId = null; //String directoryId

            if (postList.iterator().hasNext()) {
                postRetreived = postList.iterator().next();
                directoryId = postRetreived.getPostId();//postRetreived.getDirectoryId
                LOGGER.debug("Product with productId : {} updated by Backend Microservice", directoryId);

                PostRest directoryHateoas = convertEntityToHateoasEntity(postRetreived);
                directoryHateoas.add(linkTo(methodOn(PostRestController.class).getPost(directoryHateoas.getPostId())).withSelfRel()); //getDirectoryId())).withSelfRel()
                deferredResult.setResult(new ResponseEntity<PostRest>(directoryHateoas, HttpStatus.OK));

            }
            else {
                LOGGER.debug("Product with code : {} not updated by Backend Microservice", id);
                deferredResult.setResult(new ResponseEntity<PostRest>(HttpStatus.NOT_FOUND));
            }


        }).exceptionally(ex -> {
            LOGGER.error(ex.getMessage());
            return null;
        });
        return deferredResult;
    }
    public DeferredResult<ResponseEntity<Post>> deletePost(UUID id, Long headerUserId){//////////
        DeferredResult<ResponseEntity<Post>> deferredResult = new DeferredResult<>();

        Posts postsRequest = new Posts();
        postsRequest.setHeaderUserId(headerUserId);
        postsRequest.setOperation(OperationKafka.DELETE);//Directories.DELETE
        List<Post> postRequestList = new ArrayList<>();
        Post postToDelete = new Post();
        postToDelete.setPostId(id);
        //postToDelete.setCreationDate(new Timestamp(System.currentTimeMillis()));
        //directoryToDelete.setOrder(0L);
        //postToDelete.setPostContent(null);//.setOrder(null);
        //postToDelete.setChangeDate(null);//.setSubDirId(null);
        //postToDelete.setPostId(null);
//		directoryToDelete.setName("");
//		directoryToDelete.setCode("");
//		directoryToDelete.setTitle("");
//		directoryToDelete.setPrice(0D);
        postRequestList.add(postToDelete);
        postsRequest.setPosts(postRequestList);

        CompletableFuture<Posts> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, postsRequest);

        completableFuture.thenAccept(directories -> {

            if (directories.getOperation().equals(OperationKafka.SUCCESS)) {//contentEquals
                LOGGER.debug("Product with productId : {} deleted by Backend Microservice", id);
                deferredResult.setResult(new ResponseEntity<Post>(HttpStatus.NO_CONTENT));

            }
            else {
                LOGGER.debug("Product with id : {} suspected not deleted by Backend Microservice", id);
                deferredResult.setResult(new ResponseEntity<Post>(HttpStatus.NOT_FOUND));
            }

        }).exceptionally(ex -> {
            LOGGER.error(ex.getMessage());
            return null;
        });
        return deferredResult;
    }
        private PostRest convertEntityToHateoasEntity(Post post){
        return  modelMapper.map(post,  PostRest.class);
    }

}
