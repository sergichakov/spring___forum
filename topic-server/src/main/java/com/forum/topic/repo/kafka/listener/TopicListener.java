
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

@Component

public class TopicListener {

    //	@Autowired
    private TopicRepository topicRepository;
    //	@Autowired
    private TopicPagingRepository topicPagingRepository;
    private TopicMapper mapper = TopicMapper.INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicListener.class);

    public TopicListener(TopicRepository topicRepository, TopicPagingRepository topicPagingRepository) {
        this.topicRepository = topicRepository;
        this.topicPagingRepository = topicPagingRepository;
    }

    @KafkaListener(topics = "${kafka.topic.product.request}", containerFactory = "requestReplyListenerContainerFactory")
    @SendTo
    public Topics listenConsumerRecord(ConsumerRecord<String, Topics> record) {

        long secondsToSleep = 3;

        LOGGER.info("Start");

        //print all headers
        record.headers().forEach(header -> LOGGER.debug(header.key() + ":" + new String(header.value())));

        String key = record.key();
        Topics topics = record.value();
        LOGGER.debug("Listen; key : " + key);
        LOGGER.debug("Listen; value : " + topics);

        Topics topicsToReturn = resolveAndExecute(topics);

        LOGGER.info("Ending");
        return topicsToReturn;

    }

    private Topics resolveAndExecute(Topics topics) {

        LOGGER.info("Start");
        Topics topicsToReturn = null;

        if (topics.getOperation().equals(OperationKafka.RETREIVE_DETAILS)) {
            topicsToReturn = getTopic(topics);
        } else if (topics.getOperation().equals(OperationKafka.RETREIVE_ALL)) {
            topicsToReturn = getAllTopics(topics);
        } else if (topics.getOperation().equals(OperationKafka.CREATE)) {
            topicsToReturn = createTopic(topics);
        } else if (topics.getOperation().equals(OperationKafka.UPDATE)) {
            topicsToReturn = updateTopic(topics);
        } else if (topics.getOperation().equals(OperationKafka.DELETE)) {
            topicsToReturn = deleteTopic(topics);
        } else {
            LOGGER.debug("Inside else. Undefined Operation!");
        }
        LOGGER.info("Ending");
        return topicsToReturn;
    }

    //------------------- Retreive a Topic --------------------------------------------------------
    @Transactional(readOnly = true)
    public Topics getTopic(Topics topics) {

        LOGGER.info("Start");
        Topics topicsToReturn = new Topics();
        if ((null != topics) && (topics.getTopics().iterator().hasNext())) {
            Long postId = ((Topic) topics.getTopics().iterator().next()).getPostId();
            LOGGER.debug("Fetching Product with topicId : {}", postId);
            TopicEntity topicEntity = null;
            if (topics.getHeaderUserId() == null) {
                topicEntity = topicRepository.findById((postId)).get(); //findById(Long.parseLong(directoryId)).get();
            } else {
                topicEntity = topicRepository.findByPostIdAndUserOwnerId(postId, topics.getHeaderUserId());
            }
            topicsToReturn.setOperation(OperationKafka.SUCCESS);
            if (topicEntity == null) {
                LOGGER.debug("Topic with topicId : {} not found in repository", postId);
            } else {
                LOGGER.debug("Topic with topicId : {} found in repository", postId);
                List<Topic> topicListToReturn = new ArrayList<Topic>();
                topicListToReturn.add(mapper.entityToApi(topicEntity));
                topicsToReturn.setTopics(topicListToReturn);
            }
        } else {
            LOGGER.debug("Topic cannot be fetched, since param is null or empty");
            topicsToReturn.setOperation(OperationKafka.FAILURE);
        }
        LOGGER.info("Ending");
        return topicsToReturn;
    }

    @Transactional
    public Topics createTopic(Topics topics) {

        LOGGER.info("Start");
        Topics topicsToReturn = new Topics();
        List<Topic> topicListToReturn = null;
        Topic topicToCreate = null;
        List<TopicEntity> topicsWithPostIdFound = null;
        TopicEntity topicCreateEntity = null;
        Integer page = topics.getPage();
        Integer numberPerPage = topics.getNumberPerPage();
        if (page == null) {
            page = 0;
        }
        if (numberPerPage == null) {
            numberPerPage = 1000;
        }

        if ((null != topics) && (topics.getTopics().iterator().hasNext())) {

            topicToCreate = topics.getTopics().iterator().next();
            LOGGER.debug("Attempting to create a new Topic with code: {}", topicToCreate.getPostId());

            System.out.println("gotten Create" + lookUpFindAll());

            topicCreateEntity = topicRepository.save(mapper.apiToEntity(topicToCreate));
            LOGGER.debug("a Topic with id {} created newly", topicCreateEntity.getPostId());
            topicsToReturn.setOperation(OperationKafka.SUCCESS);
            topicListToReturn = new ArrayList<Topic>();
            topicListToReturn.add(mapper.entityToApi(topicCreateEntity));
            topicsToReturn.setTopics(topicListToReturn);
//            }
        } else {
            LOGGER.debug("Topic cannot be created, since param is null or empty");
            topicsToReturn.setOperation(OperationKafka.FAILURE);
        }
        LOGGER.info("Ending");
        return topicsToReturn;
    }

    @Transactional
    public List<TopicEntity> lookUpFindAll() {
        List<TopicEntity> listTopic = new ArrayList<>();

        for (TopicEntity topicEntity : topicRepository.findAll()) {
            listTopic.add(topicEntity);
        }
        return listTopic;
    }

    @Transactional
    public void setUpBeforeFindAll(TopicEntity topicEntity) {
        topicRepository.save(topicEntity);

    }

    @Transactional
    public Topics updateTopic(Topics topics) {

        LOGGER.info("Start of update");
        Topics topicsToReturn = new Topics();
        List<Topic> topicListToReturn = null;
        Topic topicToUpdate = null;
        TopicEntity topicFoundEntity = null;
        TopicEntity directoryUpdatedEntity = null;


        if ((null != topics) && (topics.getTopics().iterator().hasNext())) {

            topicToUpdate = topics.getTopics().iterator().next();
            LOGGER.debug("Attempting to find a Topic with id: {} to update", topicToUpdate.getPostId());
            if (topics.getHeaderUserId() == null) {
                topicFoundEntity = topicRepository.findById(topicToUpdate.getPostId()).get();
            } else {
                topicFoundEntity = topicRepository
                        .findByPostIdAndUserOwnerId(topicToUpdate.getPostId(), topics.getHeaderUserId());
            }

            if (null != topicFoundEntity) {
                LOGGER.debug("a Topic with id {} exist, attempting to update", topicFoundEntity.getPostId());

                topicsToReturn.setOperation(OperationKafka.SUCCESS);

                directoryUpdatedEntity = topicRepository.save(mapper.apiToEntity(topicToUpdate));
                topicListToReturn = new ArrayList<Topic>();
                topicListToReturn.add(mapper.entityToApi(directoryUpdatedEntity));
                topicsToReturn.setTopics(topicListToReturn);

            } else {
                LOGGER.debug("a Topic with id {} doesn't exist", topicToUpdate.getPostId());
                topicsToReturn.setOperation(OperationKafka.FAILURE);
            }
        } else {
            LOGGER.debug("Topic cannot be updated, since param is null or empty");
            topicsToReturn.setOperation(OperationKafka.FAILURE);
        }
        LOGGER.info("Ending");
        return topicsToReturn;
    }

    @Transactional
    public Topics deleteTopic(Topics topics) {

        LOGGER.info("Start");
        Topics topicsToReturn = new Topics();
        List<Topic> topicListToReturn = null;
        Topic topicToDelete = null;
        TopicEntity topicFoundEntity = null;

        if ((null != topics) && (topics.getTopics().iterator().hasNext())) {

            topicToDelete = topics.getTopics().iterator().next();
            LOGGER.debug("Attempting to find a Topic with id: {} to delete", topicToDelete.getPostId());
            if (topics.getHeaderUserId() == null) {
                topicFoundEntity = topicRepository.findById((topicToDelete.getPostId())).get();
            } else {
                topicFoundEntity = topicRepository
                        .findByPostIdAndUserOwnerId(topicToDelete.getPostId(), topics.getHeaderUserId());
            }
            if (null != topicFoundEntity) {
                LOGGER.debug("a Topic with id {} exist, attempting to delete", topicFoundEntity.getPostId());

                topicsToReturn.setOperation(OperationKafka.SUCCESS);

                topicRepository.delete(mapper.apiToEntity(topicToDelete));
                topicsToReturn.setOperation(OperationKafka.SUCCESS);
                topicListToReturn = new ArrayList<Topic>();
                topicsToReturn.setTopics(topicListToReturn);

            } else {
                LOGGER.debug("a Topic with id {} doesn't exist", topicToDelete.getPostId());
                topicsToReturn.setOperation(OperationKafka.FAILURE);
            }
        } else {
            LOGGER.debug("Topic cannot be deleted, since param is null or empty");
            topicsToReturn.setOperation(OperationKafka.FAILURE);
        }
        LOGGER.info("Ending");
        return topicsToReturn;
    }

    @Transactional(readOnly = true)
    public Topics getAllTopics(Topics topics) {

        LOGGER.info("Start");
        Topics topicsToReturn = new Topics();

        Integer page = topics.getPage();
        Integer numberPerPage = topics.getNumberPerPage();
        if (page == null) {
            page = 0;
        }
        if (numberPerPage == null) {
            numberPerPage = 1000;
        }
        Iterable<TopicEntity> iterable = null;
        if (null == topics.getHeaderUserId()) {
            iterable = topicPagingRepository.findAll(PageRequest.of(page, numberPerPage));
        } else {
            iterable = topicPagingRepository.findByUserOwnerId(topics.getHeaderUserId(), PageRequest.of(page, numberPerPage));
        }
        List<Topic> topicListToReturn = new ArrayList<Topic>();
        for (TopicEntity topicEntity : iterable) {

            LOGGER.info("postEntity_" + topicEntity.toString());
            topicListToReturn.add(mapper.entityToApi(topicEntity));
        }
        if (topicListToReturn.size() == 0) {
            LOGGER.debug("No products retreived from repository");
        }
        topicListToReturn.forEach(item -> LOGGER.info(item.toString()));

        topicsToReturn.setOperation(OperationKafka.SUCCESS);
        topicsToReturn.setTopics(topicListToReturn);

        LOGGER.info("Ending");
        return topicsToReturn;
    }
}
