package com.forum.post.repo;

import com.forum.kafka.request_reply_util.CompletableFutureReplyingKafkaOperations;
import com.forum.post.kafka.event.OperationKafka;
import com.forum.post.kafka.event.Post;
import com.forum.post.kafka.event.Posts;
import com.forum.post.repo.model.PostEntity;
import com.forum.post.repo.repository.PostRepository;
//import com.forum.topic.kafka.event.OperationKafka;
//import com.forum.topic.kafka.event.Topic;
//import com.forum.topic.kafka.event.Posts;
//import com.forum.topic.repo.model.TopicEntity;
//import com.forum.topic.repo.repository.TopicRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@TestPropertySource(properties = {"spring.config.location=classpath:application-properties.yml"})
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EmbeddedKafka(partitions = 2, count = 1, controlledShutdown = true, topics = {"${kafka.topic.product.request}", "${kafka.topic.product.reply}"})
// partitions=1
@SpringBootTest(properties = "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}")

public class KafkaPostServerIntegreationTest {
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.3")
            .withUsername("forum")
            .withPassword("ForumPassword")
            .withReuse(true)
            .withDatabaseName("postsdb");
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private CompletableFutureReplyingKafkaOperations<String, Posts, Posts> replyKafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private PostRepository postRepository;
    @Value("${kafka.topic.product.request}")
    private String requestPost;

    @Value("${kafka.topic.product.reply}")
    private String requestReplyPost;

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
        for (PostEntity postEntityBefore : lookUpFindAll()) {
            postRepository.delete(postEntityBefore);
        }
        PostEntity postEntity = new PostEntity();
        postEntity.setTopicId(444L);
        postEntity.setPostContent("It is a new Post");
        postEntity.setUserOwnerId(111L);
        postRepository.save(postEntity);
    }

    @Test
    public void shouldReturnTopicEntity_whenRetrieveAllDataFromKafka() throws Exception {
        Posts postsRequest = new Posts();
        postsRequest.setHeaderUserId(111L);
        postsRequest.setPage(0);
        postsRequest.setNumberPerPage(1000);
        postsRequest.setOperation(OperationKafka.RETREIVE_ALL);
        Posts actualTopics = replyKafkaTemplate.requestReply(requestPost, postsRequest).get();

        Post onePost = actualTopics.getPosts().get(0);

        assertEquals(444L, onePost.getTopicId());
        assertEquals("It is a new Post", onePost.getPostContent());
        assertEquals(111L, onePost.getUserOwnerId());
    }

    @Test
    public void shouldReturnOneTopicEntity_whenRetrieveOneDataFromKafka() throws Exception {
        PostEntity postInitialEntity = lookUpFindAll().get(0);
        UUID postId = postInitialEntity.getPostId();
        Post postInside = new Post();

        postInside.setPostId(postId);
        Posts postsRequest = new Posts();
        postsRequest.setHeaderUserId(111L);
        postsRequest.setPage(0);
        postsRequest.setNumberPerPage(1000);
        postsRequest.setOperation(OperationKafka.RETREIVE_DETAILS);
        postsRequest.setPosts(List.of(postInside));
        //Action
        Posts actualPosts = replyKafkaTemplate.requestReply(requestPost, postsRequest).get();

        Post onePost = actualPosts.getPosts().get(0);
        assertEquals(postId, onePost.getPostId());
        assertEquals(444L, onePost.getTopicId());
        assertEquals("It is a new Post", onePost.getPostContent());
        assertEquals(111L, onePost.getUserOwnerId());
    }

    @Test
    @Commit
    public void shouldUpdateEntityInDb_whenEditData() throws Exception {
        //arrange
        PostEntity postInitialEntity = lookUpFindAll().get(0);
        UUID postInitialPostId = postInitialEntity.getPostId();
        Post post = new Post();
        post.setPostId(postInitialPostId);
        post.setTopicId(999L);
        post.setPostContent("It is new label");
        post.setUserOwnerId(111L);
        Posts postsRequestEdition = new Posts();
        postsRequestEdition.setHeaderUserId(111L);
        postsRequestEdition.setPage(0);
        postsRequestEdition.setNumberPerPage(1000);
        postsRequestEdition.setOperation(OperationKafka.UPDATE);
        postsRequestEdition.setPosts(List.of(post));
        //act
        Posts actualPosts = replyKafkaTemplate.requestReply(requestPost, postsRequestEdition).get();

        System.out.println("Operation of " + actualPosts.getOperation());

        PostEntity postEntity = lookUpFindAll().get(0);
        assertEquals(OperationKafka.SUCCESS, actualPosts.getOperation());
        assertEquals(999L, postEntity.getTopicId());
        assertEquals("It is new label", postEntity.getPostContent());
        assertEquals(111L, postEntity.getUserOwnerId());
        assertEquals(postInitialPostId, postEntity.getPostId());
    }

    @Transactional
    @Commit
    public List<PostEntity> lookUpFindAll() {
        List<PostEntity> listPost = new ArrayList<>();

        for (PostEntity postEntity : postRepository.findAll()) {
            listPost.add(postEntity);
        }
        return listPost;
    }

    @Test
    @Commit
    public void shouldCreateEntityInDb_whenCreateNewData() throws Exception {
        PostEntity postEntityInitial = lookUpFindAll().get(0);
        postRepository.delete(postEntityInitial);
        //arrange
        Post post = new Post();
        post.setTopicId(444L);
        post.setPostContent("It is new label");
        post.setUserOwnerId(222L);
        Posts postsRequestEdition = new Posts();
        postsRequestEdition.setHeaderUserId(222L);
        postsRequestEdition.setPage(0);
        postsRequestEdition.setNumberPerPage(1000);
        postsRequestEdition.setOperation(OperationKafka.CREATE);
        postsRequestEdition.setPosts(List.of(post));
        //act
        Posts actualPosts = replyKafkaTemplate.requestReply(requestPost, postsRequestEdition).get();

        System.out.println("Operation of " + actualPosts.getOperation());

        assertEquals(OperationKafka.SUCCESS, actualPosts.getOperation());
        PostEntity postEntity = lookUpFindAll().get(0);
        assertEquals(444L, postEntity.getTopicId());
        assertEquals("It is new label", postEntity.getPostContent());
        assertEquals(222L, postEntity.getUserOwnerId());
    }

    @Test
    @Commit
    public void shouldDeleteEntityInDb_whenDeleteData() throws Exception {
        //arrange
        PostEntity postInitialEntity = lookUpFindAll().get(0);
        UUID postInitialPostId = postInitialEntity.getPostId();
        Post post = new Post();
        post.setPostId(postInitialPostId);
        post.setUserOwnerId(111L);
        Posts postsRequestEdition = new Posts();
        postsRequestEdition.setHeaderUserId(111L);
        postsRequestEdition.setPage(0);
        postsRequestEdition.setNumberPerPage(1000);
        postsRequestEdition.setOperation(OperationKafka.DELETE);
        postsRequestEdition.setPosts(List.of(post));
        //act
        Posts actualPosts = replyKafkaTemplate.requestReply(requestPost, postsRequestEdition).get();

        System.out.println("Operation of " + actualPosts.getOperation());

        assertEquals(OperationKafka.SUCCESS, actualPosts.getOperation());
    }
}
