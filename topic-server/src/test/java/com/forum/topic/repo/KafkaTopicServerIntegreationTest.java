package com.forum.topic.repo;

import com.forum.kafka.request_reply_util.CompletableFutureReplyingKafkaOperations;
import com.forum.topic.kafka.event.OperationKafka;
import com.forum.topic.kafka.event.Topic;
import com.forum.topic.kafka.event.Topics;
import com.forum.topic.repo.model.TopicEntity;
import com.forum.topic.repo.repository.TopicRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@TestPropertySource(properties = {"spring.config.location=classpath:application-properties.yml"})
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EmbeddedKafka(partitions = 2, count = 1, controlledShutdown = true, topics = {"${kafka.topic.product.request}",
        "${kafka.topic.product.reply}"})
@SpringBootTest(properties = "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}")

public class KafkaTopicServerIntegreationTest {
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.3")
            .withUsername("forum")
            .withPassword("ForumPassword")
            .withReuse(true)
            .withDatabaseName("topicsdb");
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private CompletableFutureReplyingKafkaOperations<String, Topics, Topics> replyKafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private TopicRepository topicRepository;
    @Value("${kafka.topic.product.request}")
    private String requestTopic;

    @Value("${kafka.topic.product.reply}")
    private String requestReplyTopic;

    @AfterAll
    static void afterAll() {
        postgreSQLContainer.stop();
    }

    static {
        postgreSQLContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        log.info("jdbc url postgreSQLContainer = " + postgreSQLContainer.getJdbcUrl());
        log.info("jdbc host=" + postgreSQLContainer.getHost());
        log.info("jdbc boundPortNumbers= " + postgreSQLContainer.getBoundPortNumbers());
    }


    @BeforeAll
    public void setUp() throws Exception {

        System.setProperty("spring.kafka.bootstrap-servers", embeddedKafkaBroker.getBrokersAsString());
        for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry
                .getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer,
                    embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @BeforeEach
    @Transactional
    @Commit
    public void setupTopicsRepository() {
        for (TopicEntity topicEntityBefore : lookUpFindAll()) {
            topicRepository.delete(topicEntityBefore);
        }
        TopicEntity topicEntity = new TopicEntity();
        topicEntity.setTopicLabel("Label of topic");
        topicEntity.setPostContent("It is a new Post");
        topicEntity.setDirectoryId(333L);
        topicEntity.setUserOwnerId(111L);
        topicRepository.save(topicEntity);
    }

    @Test
    public void shouldReturnTopicEntity_whenRetrieveAllDataFromKafka() throws Exception {
        Topics topicsRequest = new Topics();
        topicsRequest.setHeaderUserId(111L);
        topicsRequest.setPage(0);
        topicsRequest.setNumberPerPage(1000);
        topicsRequest.setOperation(OperationKafka.RETREIVE_ALL);
        Topics actualTopics = replyKafkaTemplate.requestReply(requestTopic, topicsRequest).get();


        Topic oneTopic = actualTopics.getTopics().get(0);

        assertEquals("Label of topic", oneTopic.getTopicLabel());
        assertEquals("It is a new Post", oneTopic.getPostContent());
        assertEquals(333L, oneTopic.getDirectoryId());
        assertEquals(111L, oneTopic.getUserOwnerId());
    }

    @Test
    public void shouldReturnOneTopicEntity_whenRetrieveOneDataFromKafka() throws Exception {
        TopicEntity topicInitialEntity = lookUpFindAll().get(0);
        Long postId = topicInitialEntity.getPostId();
        Topic topicInside = new Topic();

        topicInside.setPostId(postId);
        Topics topicsRequest = new Topics();
        topicsRequest.setHeaderUserId(111L);
        topicsRequest.setPage(0);
        topicsRequest.setNumberPerPage(1000);
        topicsRequest.setOperation(OperationKafka.RETREIVE_DETAILS);
        topicsRequest.setTopics(List.of(topicInside));
        //Action
        Topics actualTopics = replyKafkaTemplate.requestReply(requestTopic, topicsRequest).get();


        Topic oneTopic = actualTopics.getTopics().get(0);
        assertEquals(postId, oneTopic.getPostId());
        assertEquals("Label of topic", oneTopic.getTopicLabel());
        assertEquals("It is a new Post", oneTopic.getPostContent());
        assertEquals(333L, oneTopic.getDirectoryId());
        assertEquals(111L, oneTopic.getUserOwnerId());
    }

    @Test
    @Commit
    public void shouldUpdateEntityInDb_whenEditData() throws Exception {
        //arrange
        TopicEntity topicInitialEntity = lookUpFindAll().get(0);
        Long topicInitialPostId = topicInitialEntity.getPostId();
        Topic topic = new Topic();
        topic.setPostId(topicInitialPostId);
        topic.setTopicLabel("Label of changes in original");
        topic.setPostContent("It is new label");
        topic.setUserOwnerId(111L);
        Topics topicsRequestEdition = new Topics();
        topicsRequestEdition.setHeaderUserId(111L);
        topicsRequestEdition.setPage(0);
        topicsRequestEdition.setNumberPerPage(1000);
        topicsRequestEdition.setOperation(OperationKafka.UPDATE);
        topicsRequestEdition.setTopics(List.of(topic));
        //act
        Topics actualTopics = replyKafkaTemplate.requestReply(requestTopic, topicsRequestEdition).get();

        System.out.println("Operation of " + actualTopics.getOperation());

        TopicEntity topicEntity = lookUpFindAll().get(0);
        assertEquals(OperationKafka.SUCCESS, actualTopics.getOperation());
        assertEquals("Label of changes in original", topicEntity.getTopicLabel());
        assertEquals("It is new label", topicEntity.getPostContent());
        assertEquals(111L, topicEntity.getUserOwnerId());
        assertEquals(topicInitialPostId, topicEntity.getPostId());
    }

    @Transactional
    @Commit
    public List<TopicEntity> lookUpFindAll() {
        List<TopicEntity> listTopic = new ArrayList<>();

        for (TopicEntity topicEntity : topicRepository.findAll()) {
            listTopic.add(topicEntity);
        }
        return listTopic;
    }

    @Test
    @Commit
    public void shouldCreateEntityInDb_whenCreateNewData() throws Exception {
        TopicEntity topicEntityInitial = lookUpFindAll().get(0);
        topicRepository.delete(topicEntityInitial);
        //arrange
        Topic topic = new Topic();
        topic.setTopicLabel("Label new TopicEntity");
        topic.setPostContent("It is new label");
        topic.setUserOwnerId(222L);
        Topics topicsRequestEdition = new Topics();
        topicsRequestEdition.setHeaderUserId(222L);
        topicsRequestEdition.setPage(0);
        topicsRequestEdition.setNumberPerPage(1000);
        topicsRequestEdition.setOperation(OperationKafka.CREATE);
        topicsRequestEdition.setTopics(List.of(topic));
        //act
        Topics actualTopics = replyKafkaTemplate.requestReply(requestTopic, topicsRequestEdition).get();

        System.out.println("Operation of " + actualTopics.getOperation());

        assertEquals(OperationKafka.SUCCESS, actualTopics.getOperation());
        TopicEntity topicEntity = lookUpFindAll().get(0);
        assertEquals("Label new TopicEntity", topicEntity.getTopicLabel());
        assertEquals("It is new label", topicEntity.getPostContent());
        assertEquals(222L, topicEntity.getUserOwnerId());
    }

    @Test
    @Commit
    public void shouldDeleteEntityInDb_whenDeleteData() throws Exception {
        //arrange
        TopicEntity topicInitialEntity = lookUpFindAll().get(0);
        Long topicInitialPostId = topicInitialEntity.getPostId();
        Topic topic = new Topic();
        topic.setPostId(topicInitialPostId);
        topic.setUserOwnerId(111L);
        Topics topicsRequestEdition = new Topics();
        topicsRequestEdition.setHeaderUserId(111L);
        topicsRequestEdition.setPage(0);
        topicsRequestEdition.setNumberPerPage(1000);
        topicsRequestEdition.setOperation(OperationKafka.DELETE);
        topicsRequestEdition.setTopics(List.of(topic));
        //act
        Topics actualTopics = replyKafkaTemplate.requestReply(requestTopic, topicsRequestEdition).get();

        System.out.println("Operation of " + actualTopics.getOperation());

        assertEquals(OperationKafka.SUCCESS, actualTopics.getOperation());
    }
}
