
package com.forum.topic.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forum.jwk.service.JwtService;
import com.forum.kafka.request_reply_util.CompletableFutureReplyingKafkaOperations;
import com.forum.topic.kafka.event.Topic;
import com.forum.topic.kafka.event.Topics;
import com.forum.topic.kafka.event.UserDetailsRole;
import com.forum.topic.web.hateoas.model.TopicRest;
import com.forum.topic.web.model.TopicWebDto;
import com.forum.topic.web.service.TopicWebService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Base64;
import java.util.concurrent.ExecutionException;

@CrossOrigin
@RestController
public class TopicRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicRestController.class);

    @Autowired
    private CompletableFutureReplyingKafkaOperations<String, Topics, Topics> replyingKafkaTemplate;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TopicWebService topicWebService;

    @Autowired
    private JwtService jwtService;

    @RequestMapping(value = "/topicsweb", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public DeferredResult<ResponseEntity<CollectionModel<TopicRest>>>
    getAllPosts(@RequestParam(required = false) Integer page,
                @RequestParam(required = false) Integer numberPerPage,
                @RequestParam(required = false) String allusers,
                @RequestHeader(name = "Authorization", required = false) String token) {
        Boolean allUserTopicsBool = false;
        if (allusers != null && allusers.equals("true")) {
            allUserTopicsBool = true;
        }
        LOGGER.info("Start topicWeb");
        String userId = getHeaderUserId(token, "userId");
        if (null == userId) {
            LOGGER.info("JWT token Id is null");
            DeferredResult<ResponseEntity<CollectionModel<TopicRest>>> deferredResult = new DeferredResult<>();
            deferredResult.setResult(new ResponseEntity<CollectionModel<TopicRest>>(HttpStatus.UNAUTHORIZED));
            return deferredResult;
            //throw new org.springframework.security.access.AccessDeniedException("Should have Authorization Bearer header");
        }

        Long headerUserId = Long.parseLong(userId);
        String headerRoleString = getHeaderUserId(token, "role");

        if (headerRoleString == null) {
            LOGGER.info("headerUserId or UserDetailsRole is null");
            DeferredResult<ResponseEntity<CollectionModel<TopicRest>>> deferredResult = new DeferredResult<>();
            deferredResult.setResult(new ResponseEntity<CollectionModel<TopicRest>>(HttpStatus.UNAUTHORIZED));
            return deferredResult;
        }
        UserDetailsRole headerUserRole = UserDetailsRole.valueOf(headerRoleString);
        DeferredResult<ResponseEntity<CollectionModel<TopicRest>>> deferredResult =
                topicWebService.listTopic(page, numberPerPage, headerUserId, headerUserRole, allUserTopicsBool);

        LOGGER.info("Ending");
        return deferredResult;
    }

    @RequestMapping(value = "/topicsweb/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<TopicRest>> getTopic(
            @PathVariable("id") Long id,
            @RequestHeader(name = "Authorization", required = false) String token) {

        LOGGER.info("Start");
        LOGGER.debug("Fetching Product with id: {}", id);
        LOGGER.info("Thread : " + Thread.currentThread());
        String userId = getHeaderUserId(token, "userId");
        if (null == userId) {
            LOGGER.info("JWT token Id is null");
            DeferredResult<ResponseEntity<TopicRest>> deferredResult = new DeferredResult<>();
            deferredResult.setResult(new ResponseEntity<TopicRest>(HttpStatus.UNAUTHORIZED));
            return deferredResult;
            //throw new org.springframework.security.access.AccessDeniedException("Should have Authorization Bearer header");
        }
        Long headerUserId = Long.parseLong(userId);
        String headerRoleString = getHeaderUserId(token, "role");
        if (headerRoleString == null) {
            LOGGER.info("headerUserId or UserDetailsRole is null");
            DeferredResult<ResponseEntity<TopicRest>> deferredResult = new DeferredResult<>();
            deferredResult.setResult(new ResponseEntity<TopicRest>(HttpStatus.UNAUTHORIZED));
            return deferredResult;
        }
        UserDetailsRole headerUserRole = UserDetailsRole.valueOf(headerRoleString);
        DeferredResult<ResponseEntity<TopicRest>> deferredResult
                = topicWebService.getTopic(id, headerUserId, headerUserRole);

        LOGGER.info("Ending");
        return deferredResult;
    }

    @RequestMapping(value = "/topicsweb", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<TopicRest>> addPost(@RequestBody TopicWebDto topicDto,
             @RequestHeader(name = "Authorization", required = false) String token)
            throws ExecutionException, InterruptedException {

        LOGGER.info("Start");
        LOGGER.debug("Creating Product with code: {}");

        LOGGER.info("Thread : " + Thread.currentThread());
        String userId = getHeaderUserId(token, "userId");
        if (null == userId) {
            LOGGER.info("JWT token Id is null");
            DeferredResult<ResponseEntity<TopicRest>> deferredResult = new DeferredResult<>();
            deferredResult.setResult(new ResponseEntity<TopicRest>(HttpStatus.UNAUTHORIZED));
            return deferredResult;
            //throw new org.springframework.security.access.AccessDeniedException("Should have Authorization Bearer header");
        }
        Long headerUserId = Long.parseLong(userId);
        DeferredResult<ResponseEntity<TopicRest>> deferredResult = topicWebService.createTopic(topicDto, headerUserId);
        LOGGER.info("Ending");
        return deferredResult;
    }

    @RequestMapping(value = "/topicsweb/{postId}", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<TopicRest>>
    updateDirectory(@PathVariable("postId") Long id, @RequestBody TopicWebDto topicWebDto,
                    @RequestHeader(name = "Authorization", required = false) String token) {

        LOGGER.info("Start");
        LOGGER.debug("Updating Product with id: {}", id);

        LOGGER.info("Thread : " + Thread.currentThread());
        LOGGER.info("JWT token" + token);
        String userId = getHeaderUserId(token, "userId");
        if (null == userId) {
            LOGGER.info("JWT token Id is null");
            DeferredResult<ResponseEntity<TopicRest>> deferredResult = new DeferredResult<>();
            deferredResult.setResult(new ResponseEntity<TopicRest>(HttpStatus.UNAUTHORIZED));
            return deferredResult;
            //throw new org.springframework.security.access.AccessDeniedException("Should have Authorization Bearer header");
        }
        Long headerUserId = Long.parseLong(userId);
        String headerRoleString = getHeaderUserId(token, "role");
        if (headerRoleString == null) {
            LOGGER.info("headerUserId or UserDetailsRole is null");
            DeferredResult<ResponseEntity<TopicRest>> deferredResult = new DeferredResult<>();
            deferredResult.setResult(new ResponseEntity<TopicRest>(HttpStatus.UNAUTHORIZED));
            return deferredResult;
        }
        UserDetailsRole headerUserRole = UserDetailsRole.valueOf(headerRoleString);
//        String token2 = null;
//
//        LOGGER.info("token2 = ", token2);

        DeferredResult<ResponseEntity<TopicRest>> deferredResult = topicWebService.updatePost(id,
                topicWebDto, headerUserId, headerUserRole);
        LOGGER.info("Ending");
        return deferredResult;
    }

    @RequestMapping(value = "/topicsweb/{postId}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<TopicRest>> deletePost(
            @PathVariable("postId") Long id,
            @RequestHeader(name = "Authorization", required = false) String token) { // String Id

        LOGGER.info("Start");
        LOGGER.debug("Deleting Product with id: {}", id);
//        Long headerUserId = Long.parseLong(getHeaderUserId(token, "userId"));
        String userId = getHeaderUserId(token, "userId");
        if (null == userId) {
            LOGGER.info("JWT token Id is null");
            DeferredResult<ResponseEntity<TopicRest>> deferredResult = new DeferredResult<>();
            deferredResult.setResult(new ResponseEntity<TopicRest>(HttpStatus.UNAUTHORIZED));
            return deferredResult;
            //throw new org.springframework.security.access.AccessDeniedException("Should have Authorization Bearer header");
        }
        Long headerUserId = Long.parseLong(userId);
//        UserDetailsRole headerUserRole = UserDetailsRole.valueOf(getHeaderUserId(token, "role"));
        String headerRoleString = getHeaderUserId(token, "role");
        if (headerRoleString == null) {
            LOGGER.info("headerUserId or UserDetailsRole is null");
            DeferredResult<ResponseEntity<TopicRest>> deferredResult = new DeferredResult<>();
            deferredResult.setResult(new ResponseEntity<TopicRest>(HttpStatus.UNAUTHORIZED));
            return deferredResult;
        }
        UserDetailsRole headerUserRole = UserDetailsRole.valueOf(headerRoleString);
        LOGGER.info("Thread : " + Thread.currentThread());
        DeferredResult<ResponseEntity<TopicRest>> deferredResult = topicWebService.deletePost(id, headerUserId, headerUserRole);

        LOGGER.info("Ending");
        return deferredResult;
    }

    private TopicRest convertEntityToHateoasEntity(Topic topic) {
        return modelMapper.map(topic, TopicRest.class);
    }

    private String getHeaderUserId(String token, String headerName) {
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
        } catch (JsonMappingException e) {

            LOGGER.debug("JsonMappingException has been thrown. Trouble in JWT payload");
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            LOGGER.debug("JsonProcessingException has been thrown. Trouble in JWT payload");
            e.printStackTrace();
        }

        String header = jsonNode.get(headerName).asText();

        LOGGER.debug("now i know userId {}", header);
        if (null == header || header.isEmpty()) {
            LOGGER.debug("headerUserId is empty");
            return null;
        }
        return header;
    }
}
