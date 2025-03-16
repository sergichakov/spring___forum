/*
 * Copyright (c) 2024/2025 Binildas A Christudas & Apress
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forum.post.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forum.post.kafka.event.Post;
import com.forum.post.kafka.event.Posts;

//import se.callista.blog.synch_kafka.request_reply_util.CompletableFutureReplyingKafkaOperations;

import com.forum.post.web.hateoas.model.PostRest;
import com.forum.post.web.model.PostWebDto;
import com.forum.post.web.service.PostWebService;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
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

/**
 * @author <a href="mailto:biniljava<[@.]>yahoo.co.in">Binildas C. A.</a>
 */
//@CrossOrigin

//@RestController
/* class DirectoryRestController222222222222222222222222222222222 {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryRestController222222222222222222222222222222222.class);

    @Autowired
    private CompletableFutureReplyingKafkaOperations<String, Directories, Directories> replyingKafkaTemplate;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${kafka.topic.product.request}")
    private String requestTopic;

    @Value("${kafka.topic.product.reply}")
    private String requestReplyTopic;
    @RequestMapping(value = "/directoryweb", method = RequestMethod.GET ,produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Directory> getAllDirectories(){
		LOGGER.info("Start");
		DeferredResult<ResponseEntity<CollectionModel<PostRest>>> deferredResult = new DeferredResult<>();

		Directories directoriesRequest = new Directories();
		directoriesRequest.setOperation(OperationKafka.RETREIVE_ALL); //Directories.RETREIVE_ALL)

		CompletableFuture<Directories> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, directoriesRequest);
		List <Directory> directories1=new ArrayList<>();
		completableFuture.thenAccept(directories -> {
			LOGGER.info("added directories");
            for(Directory d:directories.getDirectories()){
                LOGGER.info("received directory"+ d);
            }
			directories1.addAll(directories.getDirectories());

		});
		return directories1;
    }
	@RequestMapping(value = "/directoryweb", method = RequestMethod.GET ,produces = {MediaType.APPLICATION_JSON_VALUE})
    public DeferredResult<ResponseEntity<CollectionModel<PostRest>>> getAllDirectories(){

        LOGGER.info("Start");
        DeferredResult<ResponseEntity<CollectionModel<PostRest>>> deferredResult = new DeferredResult<>();

        Directories directoriesRequest = new Directories();
        directoriesRequest.setOperation(Directories.RETREIVE_ALL);

        CompletableFuture<Directories> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, directoriesRequest);

        completableFuture.thenAccept(directories -> {

            List<Directory> directoryList = directories.getDirectories();

            Link links[] = { linkTo(methodOn(PostRestController.class).getAllDirectories()).withSelfRel(),
                    linkTo(methodOn(PostRestController.class).getAllDirectories()).withRel("getAllDirectories") };

            List<PostRest> list = new ArrayList<PostRest>();
            for (Directory directory : directoryList) {

                PostRest directoryHateoas = convertEntityToHateoasEntity(directory);
                //list.add(directoryHateoas.add(directory.getDirectoryId())
                list.add(directoryHateoas
                        .add(linkTo(methodOn(PostRestController.class).getDirectory(directoryHateoas.getDirectoryId()))
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

        LOGGER.info("Ending");
        return deferredResult;
    }
    private PostRest convertEntityToHateoasEntity(Directory directory){
        return  modelMapper.map(directory,  PostRest.class);
    }
}
*/
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
	
	//------------------- Retreive all Products --------------------------------------------------------
    @RequestMapping(value = "/postsweb", method = RequestMethod.GET ,produces = {MediaType.APPLICATION_JSON_VALUE})
	public DeferredResult<ResponseEntity<CollectionModel<PostRest>>>
    getAllPosts(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer numberPerPage){

    	LOGGER.info("Start");
		DeferredResult<ResponseEntity<CollectionModel<PostRest>>> deferredResult =
				postWebService.listPost(page, numberPerPage);//new DeferredResult<>();
/*
		Directories directoriesRequest = new Directories();
		directoriesRequest.setPage(page);
		directoriesRequest.setNumberPerPage(numberPerPage);
		directoriesRequest.setOperation(OperationKafka.RETREIVE_ALL);  //Directories.RETREIVE_ALL)
        
        CompletableFuture<Directories> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, directoriesRequest);
        
        completableFuture.thenAccept(directories -> {
        	
        	List<Directory> directoryList = directories.getDirectories();
        	
			Link links[] = { linkTo(methodOn(PostRestController.class).getAllDirectories(page,numberPerPage)).withSelfRel(),
					linkTo(methodOn(PostRestController.class).getAllDirectories(page,numberPerPage)).withRel("getAllDirectories") };

			List<PostRest> list = new ArrayList<PostRest>();
			for (Directory directory : directoryList) {
				PostRest directoryHateoas = convertEntityToHateoasEntity(directory);
				list.add(directoryHateoas
						.add(linkTo(methodOn(PostRestController.class).getDirectory(directoryHateoas.getDirectoryId()))
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
        */
        LOGGER.info("Ending");
        return deferredResult;
    }
    
    private void delay() {
        
        long secondsToSleep = 6;
        LOGGER.debug(Thread.currentThread().toString());
        LOGGER.debug("Starting to Sleep Seconds : " + secondsToSleep);

        try{
            Thread.sleep(1000 * secondsToSleep);
        }
        catch(Exception e) {
            LOGGER.error("Error : " + e);
        }
        LOGGER.debug("Awakening from Sleep...");
 		
    }

  //------------------- Retreive a Product --------------------------------------------------------
    @RequestMapping(value = "/postsweb/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<PostRest>> getPost(@PathVariable("id") UUID id) {//String id
    	
    	LOGGER.info("Start");
    	LOGGER.debug("Fetching Product with id: {}", id);
		LOGGER.info("Thread : " + Thread.currentThread());
		DeferredResult<ResponseEntity<PostRest>> deferredResult = postWebService.getPost(id);

/*		Directories directoriesRequest = new Directories();
		directoriesRequest.setOperation(OperationKafka.RETREIVE_DETAILS);//Directories.RETREIVE_DETAILS
		Directory directory = new Directory();
		directory.setDirectoryId(id);
		List<Directory> directoryRequestList = new ArrayList<>();
		directoryRequestList.add(directory);
		directoriesRequest.setDirectories(directoryRequestList);
		
        CompletableFuture<Directories> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, directoriesRequest);
        
        completableFuture.thenAccept(directories -> {
        	
        	List<Directory> directoryList = directories.getDirectories();
        	Directory directoryRetreived = null;
        	Long directoryId = null; // String directoryId

            if (directoryList.iterator().hasNext()) {
            	directoryRetreived = directoryList.iterator().next();
            	directoryId = directoryRetreived.getDirectoryId();
            	LOGGER.debug("Product with productId : {} retreived from Backend Microservice", directoryId);

				PostRest directoryHateoas = convertEntityToHateoasEntity(directoryRetreived);
				directoryHateoas.add(linkTo(methodOn(PostRestController.class).getDirectory(directoryHateoas.getDirectoryId())).withSelfRel());

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
*/
        LOGGER.info("Ending");
        return deferredResult;
    }


    //------------------- Create a Product --------------------------------------------------------
    @RequestMapping(value = "/postsweb", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<PostRest>> addPost(@RequestBody PostWebDto postRest,
    @RequestHeader (name="Authorization") String token) throws ExecutionException, InterruptedException {//Directory directory
    	
    	LOGGER.info("Start");
    	LOGGER.debug("Creating Product with code: {}");//, directory.getCode());
    	
		LOGGER.info("Thread : " + Thread.currentThread());
        Long headerUserId= Long.parseLong(getHeaderUserId(token,"userId"));

		DeferredResult<ResponseEntity<PostRest>> deferredResult = postWebService.createPost(postRest, headerUserId);
/*		DeferredResult<ResponseEntity<PostRest>> deferredResult = new DeferredResult<>();

		Directories directoriesRequest = new Directories();
		directoriesRequest.setOperation(OperationKafka.CREATE);//Directories.CREATE
		List<Directory> directoryRequestList = new ArrayList<>();
		directoryRequestList.add(directory);
		directoriesRequest.setDirectories(directoryRequestList);
        
        CompletableFuture<Directories> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, directoriesRequest);
        
        completableFuture.thenAccept(directories -> {
        	
        	List<Directory> directoryList = directories.getDirectories();
        	Directory directoryRetreived = null;
        	Long directoryId = null; //String directoryId

            if (directoryList.iterator().hasNext()) {
            	directoryRetreived = directoryList.iterator().next();
            	directoryId = directoryRetreived.getDirectoryId();
            	LOGGER.debug("Product with productId : {} created by Backend Microservice", directoryId);

				PostRest directoryHateoas = convertEntityToHateoasEntity(directoryRetreived);
				directoryHateoas.add(linkTo(methodOn(PostRestController.class).getDirectory(directoryHateoas.getDirectoryId())).withSelfRel());
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


 */
        LOGGER.info("Ending");
        return deferredResult;
    }
    @RequestMapping(value = "/admin/postsweb", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<PostRest>> addPostAsAdmin(@RequestBody PostWebDto postWebDto) throws ExecutionException, InterruptedException {
        DeferredResult<ResponseEntity<PostRest>> deferredResult = postWebService.createPost(postWebDto, postWebDto.getUserOwnerId());
        return deferredResult;
    }

    //------------------- Update a Product --------------------------------------------------------
    @RequestMapping(value = "/postsweb/{postId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<PostRest>>
			updateDirectory(@PathVariable("postId")UUID id, @RequestBody PostWebDto post, @RequestHeader (name="Authorization") String token) {
    	
    	LOGGER.info("Start");
    	LOGGER.debug("Updating Product with id: {}", id);
        
		LOGGER.info("Thread : " + Thread.currentThread());
        LOGGER.info("JWT token"+token);
        Long headerUserId= Long.parseLong(getHeaderUserId(token,"userId"));

		DeferredResult<ResponseEntity<PostRest>> deferredResult = postWebService.updatePost(id, post, headerUserId);
/*
		Directories directoriesRequest = new Directories();
		directoriesRequest.setOperation(OperationKafka.UPDATE);//Directories.UPDATE
		List<Directory> directoryRequestList = new ArrayList<>();
		directoryRequestList.add(directory);
		directoriesRequest.setDirectories(directoryRequestList);
        
        CompletableFuture<Directories> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, directoriesRequest);
        
        completableFuture.thenAccept(directories -> {
        	
        	List<Directory> directoryList = directories.getDirectories();
        	Directory directoryRetreived = null;
        	Long directoryId = null; //String directoryId

            if (directoryList.iterator().hasNext()) {
            	directoryRetreived = directoryList.iterator().next();
            	directoryId = directoryRetreived.getDirectoryId();
            	LOGGER.debug("Product with productId : {} updated by Backend Microservice", directoryId);

				PostRest directoryHateoas = convertEntityToHateoasEntity(directoryRetreived);
				directoryHateoas.add(linkTo(methodOn(PostRestController.class).getDirectory(directoryHateoas.getDirectoryId())).withSelfRel());
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
*/
        LOGGER.info("Ending");
        return deferredResult;
    }


    //------------------- Delete a Product --------------------------------------------------------

    @RequestMapping(value = "/postsweb/{postId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<Post>> deletePost(@PathVariable("postId")UUID id, @RequestHeader (name="Authorization") String token) { // String Id
    	
    	LOGGER.info("Start");
    	LOGGER.debug("Deleting Product with id: {}", id);
        Long headerUserId= Long.parseLong(getHeaderUserId(token,"userId"));


		LOGGER.info("Thread : " + Thread.currentThread());
		DeferredResult<ResponseEntity<Post>> deferredResult = postWebService.deletePost(id, headerUserId);

/*		Directories directoriesRequest = new Directories();
		directoriesRequest.setOperation(OperationKafka.DELETE);//Directories.DELETE
		List<Directory> directoryRequestList = new ArrayList<>();
		Directory directoryToDelete = new Directory();
		directoryToDelete.setDirectoryId(id);
		directoryToDelete.setCreationDate(new Timestamp(System.currentTimeMillis()));
		//directoryToDelete.setOrder(0L);
		directoryToDelete.setOrder(null);
		directoryToDelete.setSubDirId(null);
		directoryToDelete.setTopicId(null);
//		directoryToDelete.setName("");
//		directoryToDelete.setCode("");
//		directoryToDelete.setTitle("");
//		directoryToDelete.setPrice(0D);
		directoryRequestList.add(directoryToDelete);
		directoriesRequest.setDirectories(directoryRequestList);
        
        CompletableFuture<Directories> completableFuture =  replyingKafkaTemplate.requestReply(requestTopic, directoriesRequest);
        
        completableFuture.thenAccept(directories -> {

            if (directories.getOperation().equals(OperationKafka.SUCCESS)) {//contentEquals
            	LOGGER.debug("Product with productId : {} deleted by Backend Microservice", id);
            	deferredResult.setResult(new ResponseEntity<Directory>(HttpStatus.NO_CONTENT));

            }
            else {
            	LOGGER.debug("Product with id : {} suspected not deleted by Backend Microservice", id);
            	deferredResult.setResult(new ResponseEntity<Directory>(HttpStatus.NOT_FOUND));
            }
           
        }).exceptionally(ex -> {
        	LOGGER.error(ex.getMessage());
        	return null;
        });
        */

        LOGGER.info("Ending");
        return deferredResult;
    }

	private PostRest convertEntityToHateoasEntity(Post post){
		return  modelMapper.map(post,  PostRest.class);
	}

    private String getHeaderUserId(String token, String headerName){
        if(null==token || token.isEmpty()){
            return null;
        }
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payloadJson = new String(decoder.decode(chunks[1]));
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
//        Long headerUserId = Long.parseLong(jsonNode.get("userId").asText());
        String header = jsonNode.get(headerName).asText();
//        UserDetailsRole userDetailsRole=UserDetailsRole.valueOf("role");
        LOGGER.debug("now i know userId {}", header);
        if (null==header || header.isEmpty()){
            LOGGER.debug("headerUserId is empty");
            return null;
        }
        return header;
    }
}
