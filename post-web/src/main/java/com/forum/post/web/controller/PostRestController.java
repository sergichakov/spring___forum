package com.forum.post.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forum.jwk.fetch.JwtKeyComponent;
import com.forum.jwk.service.JwtService;
import com.forum.post.kafka.event.Post;
import com.forum.post.kafka.event.Posts;

//import se.callista.blog.synch_kafka.request_reply_util.CompletableFutureReplyingKafkaOperations;

import com.forum.post.web.hateoas.model.PostRest;
import com.forum.post.web.model.PostWebDto;
import com.forum.post.web.service.PostWebService;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import org.springframework.hateoas.CollectionModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.forum.kafka.request_reply_util.CompletableFutureReplyingKafkaOperations;

import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@CrossOrigin
@RestController
public class PostRestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PostRestController.class);

	@Autowired
	private CompletableFutureReplyingKafkaOperations<String, Posts, Posts> replyingKafkaTemplate;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private PostWebService postWebService;

	@Value("${kafka.topic.product.request}")
	private String requestTopic;
	  
	@Value("${kafka.topic.product.reply}")
    private String requestReplyTopic;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private JwtKeyComponent jwtKeyComponent;
	
    @RequestMapping(value = "/postsweb", method = RequestMethod.GET ,produces = {MediaType.APPLICATION_JSON_VALUE})
	public DeferredResult<ResponseEntity<CollectionModel<PostRest>>>
    getAllPosts(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer numberPerPage){

    	LOGGER.info("Start");
		DeferredResult<ResponseEntity<CollectionModel<PostRest>>> deferredResult =
				postWebService.listPost(page, numberPerPage);//new DeferredResult<>();
        LOGGER.info("Ending");
        return deferredResult;
    }
    
    @RequestMapping(value = "/postsweb/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<PostRest>> getPost(@PathVariable("id") UUID id) {//String id
    	
    	LOGGER.info("Start");
    	LOGGER.debug("Fetching post with id: {}", id);
		LOGGER.info("Thread : " + Thread.currentThread());
		DeferredResult<ResponseEntity<PostRest>> deferredResult = postWebService.getPost(id);

        LOGGER.info("Ending");
        return deferredResult;
    }
    @RequestMapping(value = "/postsweb/topic/{topicid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<CollectionModel<PostRest>>> getAllPostsByTopicId(@PathVariable("topicid") Long topicId,
             @RequestParam (required = false) Integer page, @RequestParam(required = false) Integer numberPerPage) {//String id

        LOGGER.info("Start");
        LOGGER.debug("Fetching post with id: {}", topicId);
        LOGGER.info("Thread : " + Thread.currentThread());
        DeferredResult<ResponseEntity<CollectionModel<PostRest>>> deferredResult = postWebService.listPostsByTopicId(topicId, page, numberPerPage);

        LOGGER.info("Ending");
        return deferredResult;
    }

    @RequestMapping(value = "/postsweb", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<PostRest>> addPost(@RequestBody PostWebDto postWebDto,
    @RequestHeader (name="Authorization", required = false) String token) throws ExecutionException, InterruptedException {//Directory directory
    	
    	LOGGER.info("Start");
    	LOGGER.debug("Creating Post with code: {}");//, directory.getCode());
    	
		LOGGER.info("Thread : " + Thread.currentThread());
        String userId = getHeaderUserId(token, "userId");
        if (null == userId) {
            LOGGER.info("JWT token Id is null");
            DeferredResult<ResponseEntity<PostRest>> deferredResult = new DeferredResult<>();
            deferredResult.setResult(new ResponseEntity<PostRest>(HttpStatus.UNAUTHORIZED));
            return deferredResult;
        }
        Long headerUserId = Long.parseLong(userId);
		DeferredResult<ResponseEntity<PostRest>> deferredResult = postWebService.createPost(postWebDto, headerUserId);

        LOGGER.info("Ending");
        return deferredResult;
    }
    @RequestMapping(value = "/postweb/admin/postsweb", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<PostRest>> addPostAsAdmin(@RequestBody PostWebDto postWebDto) throws ExecutionException, InterruptedException {
        DeferredResult<ResponseEntity<PostRest>> deferredResult = postWebService.createPost(postWebDto, postWebDto.getUserOwnerId());
        return deferredResult;
    }

    @RequestMapping(value = "/postsweb/{postId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<PostRest>>
			updateDirectory(@PathVariable("postId")UUID id, @RequestBody PostWebDto post, @RequestHeader (name="Authorization", required = false) String token) {
    	
    	LOGGER.info("Start");
    	LOGGER.debug("Updating Post with id: {}", id);
        
		LOGGER.info("Thread : " + Thread.currentThread());
        LOGGER.info("JWT token"+token);
        String userId = getHeaderUserId(token, "userId");
        if (null == userId) {
            LOGGER.info("JWT token Id is null");
            DeferredResult<ResponseEntity<PostRest>> deferredResult = new DeferredResult<>();
            deferredResult.setResult(new ResponseEntity<PostRest>(HttpStatus.UNAUTHORIZED));
            return deferredResult;
        }
        Long headerUserId = Long.parseLong(userId);
		DeferredResult<ResponseEntity<PostRest>> deferredResult = postWebService.updatePost(id, post, headerUserId);

        LOGGER.info("Ending");
        return deferredResult;
    }

    @RequestMapping(value = "/postsweb/{postId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<PostRest>> deletePost(@PathVariable("postId")UUID id,
                                                           @RequestHeader (name="Authorization", required = false) String token) { // String Id
    	
    	LOGGER.info("Start");
    	LOGGER.debug("Deleting Product with id: {}", id);
        String userId = getHeaderUserId(token, "userId");
        if (null == userId) {
            LOGGER.info("JWT token Id is null");
            DeferredResult<ResponseEntity<PostRest>> deferredResult = new DeferredResult<>();
            deferredResult.setResult(new ResponseEntity<PostRest>(HttpStatus.UNAUTHORIZED));
            return deferredResult;
        }
        Long headerUserId = Long.parseLong(userId);
		LOGGER.info("Thread : " + Thread.currentThread());
		DeferredResult<ResponseEntity<PostRest>> deferredResult = postWebService.deletePost(id, headerUserId);

        LOGGER.info("Ending");
        return deferredResult;
    }

	private PostRest convertEntityToHateoasEntity(Post post){
		return  modelMapper.map(post,  PostRest.class);
	}

    private String getHeaderUserId(String token, String headerName){

        if (null == token || token.isEmpty()) {
            return null;
        }
        String jwtToken = token.split("Bearer ")[1];
        if (!jwtService.isTokenValid(jwtToken)) {
            return null;
        }
        String[] tokenChunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payloadJson = new String(decoder.decode(tokenChunks[1]));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readTree(payloadJson);
        }catch(JsonMappingException e){

            LOGGER.debug("JsonMappingException has been thrown. Trouble in JWT payload");
            e.printStackTrace();
        }catch(JsonProcessingException e){
            LOGGER.debug("JsonProcessingException has been thrown. Trouble in JWT payload");
            e.printStackTrace();
        }

        String header = jsonNode.get(headerName).asText();

        LOGGER.debug("now i know userId {}", header);
        if (null==header || header.isEmpty()){
            LOGGER.debug("headerUserId is empty");
            return null;
        }
        return header;
    }
}
