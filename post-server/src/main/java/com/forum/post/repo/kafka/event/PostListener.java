
package com.forum.post.repo.kafka.event;

import java.util.*;

import com.forum.post.kafka.event.Post;
import com.forum.post.kafka.event.Posts;
import com.forum.post.kafka.event.OperationKafka;
import com.forum.post.repo.mapper.PostMapper;
import com.forum.post.repo.model.PostEntity;
import com.forum.post.repo.repository.PostPagingRepository;
import com.forum.post.repo.repository.PostRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

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
        } else if (posts.getOperation().equals(OperationKafka.RETREIVE_TOPIC)){
            postsToReturn = getAllPostsByTopicId(posts);
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

    @Transactional
    @Cacheable(cacheNames="post")
    public Posts getPost(Posts posts) {

        LOGGER.info("Start");
        Posts postsToReturn = new Posts();
        if ((null != posts) && (posts.getPosts().iterator().hasNext())) {
            UUID postId = ((Post) posts.getPosts().iterator().next()).getPostId();
            LOGGER.debug("Fetching Post with postId : {}", postId);



            Optional<PostEntity>optionalPostFoundEntity = postRepository.findById((postId));
            if (!optionalPostFoundEntity.isPresent()) {
                LOGGER.debug("Post with postId : {} not found in repository", postId);
                postsToReturn.setOperation(OperationKafka.FAILURE);
            } else {
                PostEntity postEntity = optionalPostFoundEntity.get();
                LOGGER.debug("Post with postId : {} found in repository", postId);
                List<Post> postListToReturn = new ArrayList<Post>();
                postListToReturn.add(mapper.entityToApi(postEntity));
                postsToReturn.setPosts(postListToReturn);
                postsToReturn.setOperation(OperationKafka.SUCCESS);
            }
        } else {
            LOGGER.debug("Post cannot be fetched, since param is null or empty");
            postsToReturn.setOperation(OperationKafka.FAILURE);
        }
        LOGGER.info("Ending");
        return postsToReturn;
    }
    @Transactional
    @Cacheable(cacheNames="post")
    public Posts createPost(Posts posts) {

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
            PostEntity postEntity = mapper.apiToEntity(postToCreate);
//            postEntity.setCreationDate(null);                       //// this two fields added
//            postEntity.setChangeDate(null);
//            BeanUtils.copyProperties(postEntity, postFoundEntity, getNullPropertyNames(postEntity));

            LOGGER.debug("Attempting to create a new Post with code: {}", postToCreate.getPostId());

            postCreateEntity = postRepository.save(postEntity);
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
    @Transactional
    @CachePut(cacheNames="post")
    public Posts updatePost(Posts posts) {

        LOGGER.info("Start");
        Posts postsToReturn = new Posts();
        List<Post> postListToReturn = null;
        Post postToUpdate = null;
        PostEntity postFoundEntity = null;
        PostEntity directoryUpdatedEntity = null;


        if ((null != posts) && (posts.getPosts().iterator().hasNext())) {

            postToUpdate = posts.getPosts().iterator().next();
            LOGGER.debug("Attempting to find a Post with id: {} to update", postToUpdate.getPostId());
            if (posts.getHeaderUserId() == null) {
                Optional<PostEntity>optionalPostFoundEntity = postRepository.findById((postToUpdate.getPostId()));
                if (optionalPostFoundEntity.isPresent()){
                    postFoundEntity = optionalPostFoundEntity.get();
                }else{
                    postsToReturn.setOperation(OperationKafka.FAILURE);
                    return postsToReturn;
                }
            } else {
                postFoundEntity = postRepository.findByPostIdAndUserOwnerId(postToUpdate.getPostId(), posts.getHeaderUserId());
            }
            if (null != postFoundEntity) {
                LOGGER.debug("A Post with id {} exist, attempting to update", postFoundEntity.getPostId());
                postsToReturn.setOperation(OperationKafka.SUCCESS);
//                postToUpdate.setUserOwnerId(posts.getHeaderUserId());
//                postToUpdate.setUserCreatorId(posts.getHeaderUserId());
                PostEntity postEntity = mapper.apiToEntity(postToUpdate);
//                postEntity.setTopicId(null);
//                postEntity.setCreationDate(null);                       //// this five fields added
//                postEntity.setChangeDate(null);
//                postEntity.setUserOwnerId(null);
//                postEntity.setUserCreatorId(null);
//                PostEntity existingPostEntity = postRepository.findByPostIdAndUserOwnerId(,posts.getHeaderUserId());

                BeanUtils.copyProperties(postEntity, postFoundEntity, getNullPropertyNames(postEntity));
                directoryUpdatedEntity = postRepository.save(postFoundEntity);// was postEntity
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
    @Transactional
    @CacheEvict(cacheNames="post")
    public Posts deletePost(Posts posts) {

        LOGGER.info("Start");
        Posts postsToReturn = new Posts();
        List<Post> postListToReturn = null;
        Post postToDelete = null;
        PostEntity postFoundEntity = null;

        if ((null != posts) && (posts.getPosts().iterator().hasNext())) {

            postToDelete = posts.getPosts().iterator().next();
            LOGGER.debug("Attempting to find a Post with id: {} to delete", postToDelete.getPostId());
            if (posts.getHeaderUserId() == null) {
//                postFoundEntity = postRepository.findById((postToDelete.getPostId())).get();
                Optional<PostEntity>optionalPostFoundEntity = postRepository.findById((postToDelete.getPostId()));
                if (optionalPostFoundEntity.isPresent()){
                    postFoundEntity = optionalPostFoundEntity.get();
                }else{
                    postsToReturn.setOperation(OperationKafka.FAILURE);
                    return postsToReturn;
                }
            } else {
                postFoundEntity = postRepository.findByPostIdAndUserOwnerId(postToDelete.getPostId(), posts.getHeaderUserId());
            }
            if (null != postFoundEntity) {
                LOGGER.debug("A Post with id {} exist, attempting to delete", postFoundEntity.getPostId());
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
    @Transactional(readOnly = true)
    @Cacheable(cacheNames="post")
    public Posts getAllPosts(Posts posts) {

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
        Page<PostEntity> iterable = postPagingRepository.findAllByOrderByTopicIdAsc(PageRequest.of(page, numberPerPage));
        if(iterable.hasContent()) {
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
        }else{
            postsToReturn.setOperation(OperationKafka.FAILURE);
        }
        LOGGER.info("Ending");
        return postsToReturn;
    }
    @Transactional(readOnly = true)
    @Cacheable(cacheNames="post")
    public Posts getAllPostsByTopicId(Posts posts){
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
        Page<PostEntity> iterable = postPagingRepository.findAllByTopicIdOrderByCreationDateAsc(posts.getPosts().get(0).getTopicId(), PageRequest.of(page, numberPerPage));
        if(iterable.hasContent()) {
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
        }else{
            postsToReturn.setOperation(OperationKafka.FAILURE);
        }
        LOGGER.info("Ending");
        return postsToReturn;
    }
//    https://stackoverflow.com/questions/27818334/jpa-update-only-specific-fields
    public static String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}
