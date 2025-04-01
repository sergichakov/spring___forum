
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
//@RequiredArgsConstructor
//@NoArgsConstructor
public class PostListener {
	
	@Autowired
	private PostRepository postRepository;
	@Autowired
	private PostPagingRepository postPagingRepository;
    //@Autowired
    private PostMapper mapper = PostMapper.INSTANCE;

	private static final Logger LOGGER = LoggerFactory.getLogger(PostListener.class);

    @KafkaListener(topics = "${kafka.topic.product.request}", containerFactory = "requestReplyListenerContainerFactory")
    @SendTo
    public Posts listenConsumerRecord(ConsumerRecord<String, Posts> record){//, Consumer<String,Directories> consumerToMoveOffset){

        long secondsToSleep = 3;

        LOGGER.info("Start");

        //print all headers
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

    	if(posts.getOperation().equals(OperationKafka.RETREIVE_DETAILS)){
    		postsToReturn = getPost(posts);
    	}
    	else if(posts.getOperation().equals(OperationKafka.RETREIVE_ALL)){
    		postsToReturn = getAllPosts(posts);
    	}
    	else if(posts.getOperation().equals(OperationKafka.CREATE)){
    		postsToReturn = createPost(posts);
    	}
    	else if(posts.getOperation().equals(OperationKafka.UPDATE)){
    		postsToReturn = updatePost(posts);
    	}
    	else if(posts.getOperation().equals(OperationKafka.DELETE)){
    		postsToReturn = deletePost(posts);
    	}
    	else {
    		LOGGER.debug("Inside else. Undefined Operation!");
    	}
    	LOGGER.info("Ending");
    	return postsToReturn;
    }

    //------------------- Retreive a Product --------------------------------------------------------
    private Posts getPost(Posts posts) {

    	LOGGER.info("Start");
    	Posts postsToReturn = new Posts();
    	if((null != posts) && (posts.getPosts().iterator().hasNext())) {
    		UUID postId = ((Post) posts.getPosts().iterator().next()).getPostId();
			////((Post) posts.getPosts().iterator().next()).getDirectoryId();
    		LOGGER.debug("Fetching Product with directoryId : {}", postId);

        	PostEntity postEntity = postRepository.findById((postId)).get(); //findById(Long.parseLong(directoryId)).get();

    		postsToReturn.setOperation(OperationKafka.SUCCESS);
            if (postEntity == null) {
        		LOGGER.debug("Product with directoryId : {} not found in repository", postId);
            }
            else {
            	LOGGER.debug("Product with directoryId : {} found in repository", postId);
            	List<Post> postListToReturn = new ArrayList<Post>();
            	postListToReturn.add(mapper.entityToApi(postEntity));
            	postsToReturn.setPosts(postListToReturn);
            }
    	}
    	else {
    		LOGGER.debug("Product cannot be fetched, since param is null or empty");
    		postsToReturn.setOperation(OperationKafka.FAILURE);
    	}
    	LOGGER.info("Ending");
        return postsToReturn;
    }

    //------------------- Create a Product --------------------------------------------------------
    //
	private Posts createPost(Posts posts) {

    	LOGGER.info("Start");
    	Posts postsToReturn = new Posts();
    	List<Post> postListToReturn = null;
    	Post postToCreate = null;
    	List<PostEntity> postsFound = null;
    	PostEntity postCreateEntity = null;
//		Integer page= posts.getPage();
//		Integer numberPerPage= posts.getNumberPerPage(); //// поменять чтобы было в Directories postsToReturn.getNumberPerPage();
//		if (page==null){
//			page=0;
//		}
//		if (numberPerPage==null){
//			numberPerPage=1000;
//		}
    	
    	if((null != posts) && (posts.getPosts().iterator().hasNext())) {

    		postToCreate = posts.getPosts().iterator().next();
			postToCreate.setUserOwnerId(posts.getHeaderUserId());
			postToCreate.setUserCreatorId(posts.getHeaderUserId());
    		LOGGER.debug("Attempting to create a new Product with code: {}", postToCreate.getPostId()); //postToCreate.getName());
    		
//			postsFound = postPagingRepository.findByPostId(postToCreate.getPostId(), PageRequest.of(page,numberPerPage));
//			////postToCreate.getName(), PageRequest.of(page,numberPerPage));
//            if (postsFound.size() > 0) {
//        		LOGGER.debug("A Product with code {} already exist", postsFound.iterator().next().getPostId());////.getName());
//        		postsToReturn.setOperation(OperationKafka.FAILURE);
//            }
//            else {
				postCreateEntity = postRepository.save(mapper.apiToEntity(postToCreate));
            	LOGGER.debug("A Product with id {} created newly", postCreateEntity.getPostId());////.getDirectoryId());
            	postsToReturn.setOperation(OperationKafka.SUCCESS);
            	postListToReturn = new ArrayList<Post>();
            	postListToReturn.add(mapper.entityToApi(postCreateEntity));
            	postsToReturn.setPosts(postListToReturn);
//            }
    	}
    	else {
    		LOGGER.debug("Product cannot be created, since param is null or empty");
    		postsToReturn.setOperation(OperationKafka.FAILURE);
    	}
    	LOGGER.info("Ending");
        return postsToReturn;
    }

    //------------------- Update a Product --------------------------------------------------------
    private Posts updatePost(Posts posts) {

    	LOGGER.info("Start");
    	Posts postsToReturn = new Posts();
    	List<Post> postListToReturn = null;
    	Post postToUpdate = null;
    	PostEntity directoryFoundEntity = null;
    	PostEntity directoryUpdatedEntity = null;

    	
    	if((null != posts) && (posts.getPosts().iterator().hasNext())) {

    		postToUpdate = posts.getPosts().iterator().next();
    		LOGGER.debug("Attempting to find a Product with id: {} to update", postToUpdate.getPostId()); //postToUpdate.getDirectoryId());
    		if(posts.getHeaderUserId()==null) {
				directoryFoundEntity = postRepository.findById((postToUpdate.getPostId())).get();//findById(Long.parseLong(directoryToUpdate.getDirectoryId())).get();
			}else{
				directoryFoundEntity = postRepository.findByPostIdAndUserOwnerId(postToUpdate.getPostId(),posts.getHeaderUserId());
			}
            if (null != directoryFoundEntity) {
        		LOGGER.debug("A Product with id {} exist, attempting to update", directoryFoundEntity.getPostId());////.getDirectoryId());
				//if (directoryFoundEntity.getUserOwnerId().equals(posts.getHeaderUserId())) {
					postsToReturn.setOperation(OperationKafka.SUCCESS);
					postToUpdate.setUserOwnerId(posts.getHeaderUserId());
					postToUpdate.setUserCreatorId(posts.getHeaderUserId());
					PostEntity postEntity = mapper.apiToEntity(postToUpdate);

					directoryUpdatedEntity = postRepository.save(postEntity);
					postListToReturn = new ArrayList<Post>();
					Post post= mapper.entityToApi(directoryUpdatedEntity);

					postListToReturn.add(post);
					postsToReturn.setPosts(postListToReturn);
//				}else{
//					LOGGER.debug("A Product with from headerUserId={} doesn't match to userOwnerId={}",posts.getHeaderUserId() ,directoryFoundEntity.getUserOwnerId());
//					postsToReturn.setOperation(OperationKafka.FAILURE);
//				}
            }
            else {
            	LOGGER.debug("A Product with id {} doesn't exist", postToUpdate.getPostId());
            	postsToReturn.setOperation(OperationKafka.FAILURE);
            }
    	}
    	else {
    		LOGGER.debug("Product cannot be updated, since param is null or empty");
    		postsToReturn.setOperation(OperationKafka.FAILURE);
    	}
    	LOGGER.info("Ending");
        return postsToReturn;
    }

    //------------------- Delete a Product --------------------------------------------------------
    private Posts deletePost(Posts posts) {

    	LOGGER.info("Start");
    	Posts postsToReturn = new Posts();
    	List<Post> postListToReturn = null;
    	Post postToDelete = null;
    	PostEntity directoryFoundEntity = null;
    	
    	if((null != posts) && (posts.getPosts().iterator().hasNext())) {

    		postToDelete = posts.getPosts().iterator().next();
    		LOGGER.debug("Attempting to find a Product with id: {} to delete", postToDelete.getPostId());//postToDelete.getDirectoryId()
    		if(posts.getHeaderUserId()==null) {
				directoryFoundEntity = postRepository.findById((postToDelete.getPostId())).get(); //getDirectoryId())).get()
			}else{
				directoryFoundEntity = postRepository.findByPostIdAndUserOwnerId(postToDelete.getPostId(),posts.getHeaderUserId());
			}
            if (null != directoryFoundEntity) {
        		LOGGER.debug("A Product with id {} exist, attempting to delete", directoryFoundEntity.getPostId());////.getDirectoryId());
//				if (directoryFoundEntity.getUserOwnerId().equals(posts.getHeaderUserId())) {
					postsToReturn.setOperation(OperationKafka.SUCCESS);

					postRepository.delete(mapper.apiToEntity(postToDelete));
					postsToReturn.setOperation(OperationKafka.SUCCESS);
					postListToReturn = new ArrayList<Post>();
					postsToReturn.setPosts(postListToReturn);
//				}else{
//					LOGGER.debug("A Product with from headerUserId={} doesn't match to userOwnerId={}",posts.getHeaderUserId() ,directoryFoundEntity.getUserOwnerId());
//					postsToReturn.setOperation(OperationKafka.FAILURE);
//				}

            }
            else {
            	LOGGER.debug("A Product with id {} doesn't exist", postToDelete.getPostId());//postToDelete.getDirectoryId()
            	postsToReturn.setOperation(OperationKafka.FAILURE);
            }
    	}
    	else {
    		LOGGER.debug("Product cannot be deleted, since param is null or empty");
    		postsToReturn.setOperation(OperationKafka.FAILURE);
    	}
    	LOGGER.info("Ending");
        return postsToReturn;
    }

    //------------------- Retreive all Products --------------------------------------------------------
    private Posts getAllPosts(Posts posts) {

    	LOGGER.info("Start");
    	Posts postsToReturn = new Posts();
//		Iterable<PostEntity> iterable2 = postRepository.findAll();//// стало так потому что не ищется
//		UUID uuidTopic = UUID.fromString("0ba2c811-d89f-412f-95dd-722d95c57a16");
//		Iterable<PostEntity> iterable3 = postRepository.findByTopicId(uuidTopic);
		Integer page= posts.getPage();
		Integer numberPerPage= posts.getNumberPerPage(); //// поменять чтобы было в Directories postsToReturn.getNumberPerPage();
		if (page==null){
			page=0;
		}
		if (numberPerPage==null){
			numberPerPage=1000;
		}
		Iterable<PostEntity> iterable = postPagingRepository.findAll(PageRequest.of(page,numberPerPage));

		List<Post> postListToReturn = new ArrayList<Post> ();
        for(PostEntity postEntity :iterable){
			LOGGER.info("postEntity_"+postEntity.toString());
            postListToReturn.add(mapper.entityToApi(postEntity));
        }
        if(postListToReturn.size() == 0){
    		LOGGER.debug("No products retreived from repository");
        }       
        postListToReturn.forEach(item->LOGGER.info(item.toString())); ////LOGGER.debug(item.toString())

    	postsToReturn.setOperation(OperationKafka.SUCCESS);
    	postsToReturn.setPosts(postListToReturn);
    	
    	//delay();
    	LOGGER.info("Ending");
        return postsToReturn;
    }

    private void delay() {
        
        long secondsToSleep = 1;
        LOGGER.info("Start");
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
}
