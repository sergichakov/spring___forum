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
package com.forum.topic.repo.kafka.listener;

import java.util.List;
import java.util.ArrayList;

import com.forum.topic.kafka.event.OperationKafka;
import com.forum.topic.kafka.event.Topic;
import com.forum.topic.kafka.event.Topics;
import com.forum.topic.repo.mapper.TopicMapper;
import com.forum.topic.repo.model.TopicEntity;
import com.forum.topic.repo.repository.TopicPagingRepository;
import com.forum.topic.repo.repository.TopicRepository;
//import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:biniljava<[@.]>yahoo.co.in">Binildas C. A.</a>
 */
/*@Component
public class PostListener{

} */
@Component
//@RequiredArgsConstructor
//@NoArgsConstructor
public class TopicListener {
	
//	@Autowired
	private TopicRepository topicRepository;
//	@Autowired
	private TopicPagingRepository topicPagingRepository;
    //@Autowired
    private TopicMapper mapper = TopicMapper.INSTANCE;

	private static final Logger LOGGER = LoggerFactory.getLogger(TopicListener.class);
	public TopicListener(TopicRepository topicRepository,TopicPagingRepository topicPagingRepository){
		this.topicRepository=topicRepository;
		this.topicPagingRepository=topicPagingRepository;
	}

    @KafkaListener(topics = "${kafka.topic.product.request}", containerFactory = "requestReplyListenerContainerFactory")
    @SendTo
    public Topics listenConsumerRecord(ConsumerRecord<String, Topics> record){//, Consumer<String,Directories> consumerToMoveOffset){

        long secondsToSleep = 3;

        LOGGER.info("Start");

        //print all headers
        record.headers().forEach(header -> LOGGER.debug(header.key() + ":" + new String(header.value())));
        
        String key = record.key();
        Topics topics = record.value();
        LOGGER.debug("Listen; key : " + key);
        LOGGER.debug("Listen; value : " + topics);
//		setUpBeforeFindAll(TopicEntity.builder().topicLabel("Label from Listener").userOwnerId(123L).build());
//		System.out.println("gotten inside TopicListener "+lookUpFindAll());//// you may kill this
        Topics topicsToReturn = resolveAndExecute(topics);

        LOGGER.info("Ending");
        return topicsToReturn;
        
    }
    
    private Topics resolveAndExecute(Topics topics) {
    	
    	LOGGER.info("Start");
    	Topics topicsToReturn = null;

    	if(topics.getOperation().equals(OperationKafka.RETREIVE_DETAILS)){
    		topicsToReturn = getTopic(topics);
    	}
    	else if(topics.getOperation().equals(OperationKafka.RETREIVE_ALL)){
    		topicsToReturn = getAllTopics(topics);
    	}
    	else if(topics.getOperation().equals(OperationKafka.CREATE)){
    		topicsToReturn = createTopic(topics);
    	}
    	else if(topics.getOperation().equals(OperationKafka.UPDATE)){
    		topicsToReturn = updateTopic(topics);
    	}
    	else if(topics.getOperation().equals(OperationKafka.DELETE)){
    		topicsToReturn = deleteTopic(topics);
    	}
    	else {
    		LOGGER.debug("Inside else. Undefined Operation!");
    	}
    	LOGGER.info("Ending");
    	return topicsToReturn;
    }

    //------------------- Retreive a Product --------------------------------------------------------
	@Transactional(readOnly = true)
	public Topics getTopic(Topics topics) {

    	LOGGER.info("Start");
    	Topics topicsToReturn = new Topics();
    	if((null != topics) && (topics.getTopics().iterator().hasNext())) {
    		Long postId = ((Topic) topics.getTopics().iterator().next()).getPostId();
			////((Post) posts.getPosts().iterator().next()).getDirectoryId();
    		LOGGER.debug("Fetching Product with directoryId : {}", postId);
			TopicEntity topicEntity = null;
			if(topics.getHeaderUserId()==null) {
				topicEntity = topicRepository.findById((postId)).get(); //findById(Long.parseLong(directoryId)).get();
			}else{
				topicEntity = topicRepository.findByPostIdAndUserOwnerId(postId, topics.getHeaderUserId());
			}
    		topicsToReturn.setOperation(OperationKafka.SUCCESS);
            if (topicEntity == null) {
        		LOGGER.debug("Product with directoryId : {} not found in repository", postId);
            }
            else {
//				if (topicEntity.getUserOwnerId().equals(topics.getHeaderUserId())
//						|| topics.getRole().equals(UserDetailsRole.ROLE_ADMIN)) {

					LOGGER.debug("Product with directoryId : {} found in repository", postId);
					List<Topic> topicListToReturn = new ArrayList<Topic>();
					topicListToReturn.add(mapper.entityToApi(topicEntity));
					topicsToReturn.setTopics(topicListToReturn);
//				}else{
//				LOGGER.debug("A Product with from headerUserId={} doesn't match to userOwnerId={}", topics.getHeaderUserId() ,topicEntity.getUserOwnerId());
//				topicsToReturn.setOperation(OperationKafka.FAILURE);
//				}
            }
    	}
    	else {
    		LOGGER.debug("Product cannot be fetched, since param is null or empty");
    		topicsToReturn.setOperation(OperationKafka.FAILURE);
    	}
    	LOGGER.info("Ending");
        return topicsToReturn;
    }

    //------------------- Create a Product --------------------------------------------------------
    //
	@Transactional
	public Topics createTopic(Topics topics) {

    	LOGGER.info("Start");
    	Topics topicsToReturn = new Topics();
    	List<Topic> topicListToReturn = null;
    	Topic topicToCreate = null;
    	List<TopicEntity> topicsWithPostIdFound = null;
    	TopicEntity topicCreateEntity = null;
		Integer page= topics.getPage();
		Integer numberPerPage= topics.getNumberPerPage(); //// поменять чтобы было в Directories postsToReturn.getNumberPerPage();
		if (page==null){
			page=0;
		}
		if (numberPerPage==null){
			numberPerPage=1000;
		}
    	
    	if((null != topics) && (topics.getTopics().iterator().hasNext())) {

    		topicToCreate = topics.getTopics().iterator().next();
    		LOGGER.debug("Attempting to create a new Product with code: {}", topicToCreate.getPostId()); //postToCreate.getName());
    		
//			topicsWithPostIdFound = topicPagingRepository.findByPostId(topicToCreate.getPostId(), PageRequest.of(page,numberPerPage));
			////postToCreate.getName(), PageRequest.of(page,numberPerPage));
			System.out.println("gotten Create"+lookUpFindAll());
//            if (topicsWithPostIdFound.size() > 0) {
//        		LOGGER.debug("A Product with code {} already exist", topicsWithPostIdFound.iterator().next().getPostId());////.getName());
//        		topicsToReturn.setOperation(OperationKafka.FAILURE);
//            }
//            else {
				topicCreateEntity = topicRepository.save(mapper.apiToEntity(topicToCreate));
            	LOGGER.debug("A Product with id {} created newly", topicCreateEntity.getPostId());////.getDirectoryId());
            	topicsToReturn.setOperation(OperationKafka.SUCCESS);
            	topicListToReturn = new ArrayList<Topic>();
            	topicListToReturn.add(mapper.entityToApi(topicCreateEntity));
            	topicsToReturn.setTopics(topicListToReturn);
//            }
    	}
    	else {
    		LOGGER.debug("Product cannot be created, since param is null or empty");
    		topicsToReturn.setOperation(OperationKafka.FAILURE);
    	}
    	LOGGER.info("Ending");
        return topicsToReturn;
    }
	@Transactional
	public List<TopicEntity> lookUpFindAll(){
		List<TopicEntity> listTopic= new ArrayList<>();

		for (TopicEntity topicEntity : topicRepository.findAll()){
			listTopic.add(topicEntity);
		}
		return listTopic;
	}
	@Transactional
	public void setUpBeforeFindAll(TopicEntity topicEntity){
		topicRepository.save(topicEntity);
	}
    //------------------- Update a Product --------------------------------------------------------
	@Transactional
	public Topics updateTopic(Topics topics) {

    	LOGGER.info("Start of update");
    	Topics topicsToReturn = new Topics();
    	List<Topic> topicListToReturn = null;
    	Topic topicToUpdate = null;
    	TopicEntity topicFoundEntity = null;
    	TopicEntity directoryUpdatedEntity = null;

    	
    	if((null != topics) && (topics.getTopics().iterator().hasNext())) {

    		topicToUpdate = topics.getTopics().iterator().next();
    		LOGGER.debug("Attempting to find a Product with id: {} to update", topicToUpdate.getPostId()); //postToUpdate.getDirectoryId());
    		if(topics.getHeaderUserId()==null) {
				topicFoundEntity = topicRepository.findById(topicToUpdate.getPostId()).get();//findById(Long.parseLong(directoryToUpdate.getDirectoryId())).get();
			}else{
				topicFoundEntity = topicRepository.findByPostIdAndUserOwnerId(topicToUpdate.getPostId(), topics.getHeaderUserId());
			}
//			Iterable<TopicEntity> iterToEntKill = topicRepository.findAll();
//			System.out.println("TO run toEntKill");
//			for (TopicEntity toEntKill: iterToEntKill){
//				System.out.println("toEntKill "+ iterToEntKill);
//			}
            if (null != topicFoundEntity) {
        		LOGGER.debug("A Product with id {} exist, attempting to update", topicFoundEntity.getPostId());////.getDirectoryId());
//				if (topicFoundEntity.getUserOwnerId().equals(topics.getHeaderUserId())
//						|| topics.getRole().equals(UserDetailsRole.ROLE_ADMIN)) {
					topicsToReturn.setOperation(OperationKafka.SUCCESS);

					directoryUpdatedEntity = topicRepository.save(mapper.apiToEntity(topicToUpdate));
					topicListToReturn = new ArrayList<Topic>();
					topicListToReturn.add(mapper.entityToApi(directoryUpdatedEntity));
					topicsToReturn.setTopics(topicListToReturn);
//				}else{
//					LOGGER.debug("A Product with from headerUserId={} doesn't match to userOwnerId={}", topics.getHeaderUserId() ,topicFoundEntity.getUserOwnerId());
//					topicsToReturn.setOperation(OperationKafka.FAILURE);
//				}
            }
            else {
            	LOGGER.debug("A Product with id {} doesn't exist", topicToUpdate.getPostId());
            	topicsToReturn.setOperation(OperationKafka.FAILURE);
            }
    	}
    	else {
    		LOGGER.debug("Product cannot be updated, since param is null or empty");
    		topicsToReturn.setOperation(OperationKafka.FAILURE);
    	}
    	LOGGER.info("Ending");
        return topicsToReturn;
    }

    //------------------- Delete a Product --------------------------------------------------------
	@Transactional
	public Topics deleteTopic(Topics topics) {

    	LOGGER.info("Start");
    	Topics topicsToReturn = new Topics();
    	List<Topic> topicListToReturn = null;
    	Topic topicToDelete = null;
    	TopicEntity topicFoundEntity = null;
    	
    	if((null != topics) && (topics.getTopics().iterator().hasNext())) {

    		topicToDelete = topics.getTopics().iterator().next();
    		LOGGER.debug("Attempting to find a Product with id: {} to delete", topicToDelete.getPostId());//postToDelete.getDirectoryId()
    		if(topics.getHeaderUserId()==null) {
				topicFoundEntity = topicRepository.findById((topicToDelete.getPostId())).get(); //getDirectoryId())).get()
			}else{
				topicFoundEntity = topicRepository.findByPostIdAndUserOwnerId(topicToDelete.getPostId(),topics.getHeaderUserId());
			}
            if (null != topicFoundEntity) {
        		LOGGER.debug("A Product with id {} exist, attempting to delete", topicFoundEntity.getPostId());////.getDirectoryId());
//				if (topicFoundEntity.getUserOwnerId().equals(topics.getHeaderUserId())
//						|| topics.getRole().equals(UserDetailsRole.ROLE_ADMIN)) {
					topicsToReturn.setOperation(OperationKafka.SUCCESS);

					topicRepository.delete(mapper.apiToEntity(topicToDelete));
					topicsToReturn.setOperation(OperationKafka.SUCCESS);
					topicListToReturn = new ArrayList<Topic>();
					topicsToReturn.setTopics(topicListToReturn);
//				}else{
//					LOGGER.debug("A Product with from headerUserId={} doesn't match to userOwnerId={}", topics.getHeaderUserId() ,topicFoundEntity.getUserOwnerId());
//					topicsToReturn.setOperation(OperationKafka.FAILURE);
//				}

            }
            else {
            	LOGGER.debug("A Product with id {} doesn't exist", topicToDelete.getPostId());//postToDelete.getDirectoryId()
            	topicsToReturn.setOperation(OperationKafka.FAILURE);
            }
    	}
    	else {
    		LOGGER.debug("Product cannot be deleted, since param is null or empty");
    		topicsToReturn.setOperation(OperationKafka.FAILURE);
    	}
    	LOGGER.info("Ending");
        return topicsToReturn;
    }

    //------------------- Retreive all Products --------------------------------------------------------
	@Transactional(readOnly = true)
	public Topics getAllTopics(Topics topics) {

    	LOGGER.info("Start");
    	Topics topicsToReturn = new Topics();

//		Iterable<PostEntity> iterable2 = postRepository.findAll();//// стало так потому что не ищется
//		UUID uuidTopic = UUID.fromString("0ba2c811-d89f-412f-95dd-722d95c57a16");
//		Iterable<PostEntity> iterable3 = postRepository.findByTopicId(uuidTopic);
		Integer page= topics.getPage();
		Integer numberPerPage= topics.getNumberPerPage(); //// поменять чтобы было в Directories postsToReturn.getNumberPerPage();
		if (page==null){
			page=0;
		}
		if (numberPerPage==null){
			numberPerPage=1000;
		}
		Iterable<TopicEntity> iterable=null;
		if (null==topics.getHeaderUserId()) {
			iterable = topicPagingRepository.findAll(PageRequest.of(page, numberPerPage));
		}else{
			iterable = topicPagingRepository.findByUserOwnerId(topics.getHeaderUserId(),PageRequest.of(page, numberPerPage));
		}
		List<Topic> topicListToReturn = new ArrayList<Topic> ();
        for(TopicEntity topicEntity :iterable){
//			if(topics.getHeaderUserId().equals(topicEntity.getUserOwnerId())
//					|| topics.getRole().equals(UserDetailsRole.ROLE_ADMIN)) {
				LOGGER.info("postEntity_" + topicEntity.toString());
				topicListToReturn.add(mapper.entityToApi(topicEntity));
//			}else{
//				LOGGER.debug("postEntity_userOwnerId not equals to input {} Entity={}",
//						topicsToReturn.getHeaderUserId(),topicEntity);
//			}
        }
        if(topicListToReturn.size() == 0){
    		LOGGER.debug("No products retreived from repository");
        }       
        topicListToReturn.forEach(item->LOGGER.info(item.toString())); ////LOGGER.debug(item.toString())

    	topicsToReturn.setOperation(OperationKafka.SUCCESS);
    	topicsToReturn.setTopics(topicListToReturn);
    	
    	//delay();
    	LOGGER.info("Ending");
        return topicsToReturn;
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
