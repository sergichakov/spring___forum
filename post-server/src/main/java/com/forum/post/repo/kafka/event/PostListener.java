
package com.forum.post.repo.kafka.event;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import com.forum.post.kafka.event.Post;
import com.forum.post.kafka.event.Posts;
import com.forum.post.kafka.event.OperationKafka;
import com.forum.post.repo.mapper.PostMapper;
import com.forum.post.repo.model.PostEntity;
import com.forum.post.repo.repository.PostPagingRepository;
import com.forum.post.repo.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PostListener {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostPagingRepository postPagingRepository;
    private PostMapper mapper = PostMapper.INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(PostListener.class);

    @KafkaListener(topics = "${kafka.topic.product.request}", containerFactory = "requestReplyListenerContainerFactory")
    @SendTo
    public Posts listenConsumerRecord(ConsumerRecord<String, Posts> record) {//, Consumer<String,Directories> consumerToMoveOffset){
        long secondsToSleep = 3;

        LOGGER.info("Start");

        record.headers().forEach(header -> LOGGER.debug(header.key() + ":" + new String(header.value())));

        String key = record.key();
        Posts posts = record.value();
        LOGGER.debug("Listen; key : " + key);
        LOGGER.debug("Listen; value : " + posts);
        Posts postsToReturn = resolveAndExecute(posts);
        LOGGER.info("Ending");
        return postsToReturn;
    }

    private Posts resolveAndExecute(Posts posts) {

        LOGGER.info("Start");
        Posts postsToReturn = null;

        if (posts.getOperation().equals(OperationKafka.RETREIVE_DETAILS)) {
            postsToReturn = getPost(posts);
        } else if (posts.getOperation().equals(OperationKafka.RETREIVE_ALL)) {
            postsToReturn = getAllPosts(posts);
        } else if (posts.getOperation().equals(OperationKafka.CREATE)) {
            postsToReturn = createPost(posts);
        } else if (posts.getOperation().equals(OperationKafka.UPDATE)) {
            postsToReturn = updatePost(posts);
        } else if (posts.getOperation().equals(OperationKafka.DELETE)) {
            postsToReturn = deletePost(posts);
        } else {
            LOGGER.debug("Inside else. Undefined Operation!");
        }
        LOGGER.info("Ending");
        return postsToReturn;
    }

    private Posts getPost(Posts posts) {

        LOGGER.info("Start");
        Posts postsToReturn = new Posts();
        if ((null != posts) && (posts.getPosts().iterator().hasNext())) {
            UUID postId = ((Post) posts.getPosts().iterator().next()).getPostId();
            LOGGER.debug("Fetching Post with postId : {}", postId);

            PostEntity postEntity = postRepository.findById((postId)).get();

            postsToReturn.setOperation(OperationKafka.SUCCESS);
            if (postEntity == null) {
                LOGGER.debug("Post with postId : {} not found in repository", postId);
            } else {
                LOGGER.debug("Post with postId : {} found in repository", postId);
                List<Post> postListToReturn = new ArrayList<Post>();
                postListToReturn.add(mapper.entityToApi(postEntity));
                postsToReturn.setPosts(postListToReturn);
            }
        } else {
            LOGGER.debug("Post cannot be fetched, since param is null or empty");
            postsToReturn.setOperation(OperationKafka.FAILURE);
        }
        LOGGER.info("Ending");
        return postsToReturn;
    }

    private Posts createPost(Posts posts) {

        LOGGER.info("Start");
        Posts postsToReturn = new Posts();
        List<Post> postListToReturn = null;
        Post postToCreate = null;
        List<PostEntity> postsFound = null;
        PostEntity postCreateEntity = null;

        if ((null != posts) && (posts.getPosts().iterator().hasNext())) {

            postToCreate = posts.getPosts().iterator().next();
            postToCreate.setUserOwnerId(posts.getHeaderUserId());
            postToCreate.setUserCreatorId(posts.getHeaderUserId());
            LOGGER.debug("Attempting to create a new Post with code: {}", postToCreate.getPostId());

            postCreateEntity = postRepository.save(mapper.apiToEntity(postToCreate));
            LOGGER.debug("A Post with id {} created newly", postCreateEntity.getPostId());
            postsToReturn.setOperation(OperationKafka.SUCCESS);
            postListToReturn = new ArrayList<Post>();
            postListToReturn.add(mapper.entityToApi(postCreateEntity));
            postsToReturn.setPosts(postListToReturn);

        } else {
            LOGGER.debug("Post cannot be created, since param is null or empty");
            postsToReturn.setOperation(OperationKafka.FAILURE);
        }
        LOGGER.info("Ending");
        return postsToReturn;
    }

    private Posts updatePost(Posts posts) {

        LOGGER.info("Start");
        Posts postsToReturn = new Posts();
        List<Post> postListToReturn = null;
        Post postToUpdate = null;
        PostEntity directoryFoundEntity = null;
        PostEntity directoryUpdatedEntity = null;


        if ((null != posts) && (posts.getPosts().iterator().hasNext())) {

            postToUpdate = posts.getPosts().iterator().next();
            LOGGER.debug("Attempting to find a Post with id: {} to update", postToUpdate.getPostId());
            if (posts.getHeaderUserId() == null) {
                directoryFoundEntity = postRepository.findById((postToUpdate.getPostId())).get();
            } else {
                directoryFoundEntity = postRepository.findByPostIdAndUserOwnerId(postToUpdate.getPostId(), posts.getHeaderUserId());
            }
            if (null != directoryFoundEntity) {
                LOGGER.debug("A Post with id {} exist, attempting to update", directoryFoundEntity.getPostId());
                postsToReturn.setOperation(OperationKafka.SUCCESS);
                postToUpdate.setUserOwnerId(posts.getHeaderUserId());
                postToUpdate.setUserCreatorId(posts.getHeaderUserId());
                PostEntity postEntity = mapper.apiToEntity(postToUpdate);

                directoryUpdatedEntity = postRepository.save(postEntity);
                postListToReturn = new ArrayList<Post>();
                Post post = mapper.entityToApi(directoryUpdatedEntity);

                postListToReturn.add(post);
                postsToReturn.setPosts(postListToReturn);

            } else {
                LOGGER.debug("A Post with id {} doesn't exist", postToUpdate.getPostId());
                postsToReturn.setOperation(OperationKafka.FAILURE);
            }
        } else {
            LOGGER.debug("Post cannot be updated, since param is null or empty");
            postsToReturn.setOperation(OperationKafka.FAILURE);
        }
        LOGGER.info("Ending");
        return postsToReturn;
    }

    private Posts deletePost(Posts posts) {

        LOGGER.info("Start");
        Posts postsToReturn = new Posts();
        List<Post> postListToReturn = null;
        Post postToDelete = null;
        PostEntity directoryFoundEntity = null;

        if ((null != posts) && (posts.getPosts().iterator().hasNext())) {

            postToDelete = posts.getPosts().iterator().next();
            LOGGER.debug("Attempting to find a Post with id: {} to delete", postToDelete.getPostId());
            if (posts.getHeaderUserId() == null) {
                directoryFoundEntity = postRepository.findById((postToDelete.getPostId())).get();
            } else {
                directoryFoundEntity = postRepository.findByPostIdAndUserOwnerId(postToDelete.getPostId(), posts.getHeaderUserId());
            }
            if (null != directoryFoundEntity) {
                LOGGER.debug("A Post with id {} exist, attempting to delete", directoryFoundEntity.getPostId());
                postsToReturn.setOperation(OperationKafka.SUCCESS);

                postRepository.delete(mapper.apiToEntity(postToDelete));
                postsToReturn.setOperation(OperationKafka.SUCCESS);
                postListToReturn = new ArrayList<Post>();
                postsToReturn.setPosts(postListToReturn);

            } else {
                LOGGER.debug("A Product with id {} doesn't exist", postToDelete.getPostId());//postToDelete.getDirectoryId()
                postsToReturn.setOperation(OperationKafka.FAILURE);
            }
        } else {
            LOGGER.debug("Post cannot be deleted, since param is null or empty");
            postsToReturn.setOperation(OperationKafka.FAILURE);
        }
        LOGGER.info("Ending");
        return postsToReturn;
    }

    private Posts getAllPosts(Posts posts) {

        LOGGER.info("Start");
        Posts postsToReturn = new Posts();

        Integer page = posts.getPage();
        Integer numberPerPage = posts.getNumberPerPage();
        if (page == null) {
            page = 0;
        }
        if (numberPerPage == null) {
            numberPerPage = 1000;
        }
        Iterable<PostEntity> iterable = postPagingRepository.findAll(PageRequest.of(page, numberPerPage));

        List<Post> postListToReturn = new ArrayList<Post>();
        for (PostEntity postEntity : iterable) {
            LOGGER.info("postEntity_" + postEntity.toString());
            postListToReturn.add(mapper.entityToApi(postEntity));
        }
        if (postListToReturn.size() == 0) {
            LOGGER.debug("No posts retreived from repository");
        }
        postListToReturn.forEach(item -> LOGGER.info(item.toString()));

        postsToReturn.setOperation(OperationKafka.SUCCESS);
        postsToReturn.setPosts(postListToReturn);

        LOGGER.info("Ending");
        return postsToReturn;
    }
}
