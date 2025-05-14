package com.forum.post.web.controller;

import com.forum.jwk.fetch.JwtKeyComponent;
import com.forum.jwk.service.JwtService;
import com.forum.post.kafka.event.OperationKafka;
import com.forum.post.kafka.event.Post;
import com.forum.post.kafka.event.Posts;
import com.forum.post.web.model.PostWebDto;
import com.forum.post.web.service.PostWebService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

//@Disabled
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, count = 1, controlledShutdown = true)
//, kraft=true)////,brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })

@SpringBootTest(properties = {"spring.kafka.producer.bootstrap-servers-${spring.embedded.kafka.brokers}", "eureka.client.enabled=false"})
public class PostWebControllerIntegrationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostWebControllerIntegrationTest.class);

    @Autowired
    private Environment environment;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private PostWebService productService;
    private KafkaMessageListenerContainer<String, Posts> container;
    private BlockingQueue<ConsumerRecord<String, Posts>> records;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private JwtKeyComponent jwtKeyComponent;

    @BeforeAll
    void setUp() {
        Mockito.when(jwtService.isTokenValid(any())).thenReturn(true);
        DefaultKafkaConsumerFactory<String, Object> consumerFactory =
                new DefaultKafkaConsumerFactory<>(getConsumerProperties());
        ContainerProperties containerProperties =
                new ContainerProperties(environment.getProperty("product-created-events-topic-name"));
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, Posts>) records::add);
        container.start();

        LOGGER.info(" container just started");
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @AfterAll
    void tearDown() {
        container.stop();
    }

    //@Disabled
    @Test
    void whenGoPostWeb_shouldSendCreatedPostIT() throws ExecutionException, InterruptedException {
        //arrange part
        String title = "Samsung";

        PostWebDto createProductDto = new PostWebDto();
        createProductDto.setPostContent(title);
        createProductDto.setNumberOfLikes(2L);
        createProductDto.setTopicId(12345L);
        createProductDto.setUserCreatorId(100L);
        createProductDto.setUserOwnerId(111L);

        //act block
        productService.createPost(createProductDto, 1L);

        //assert block
        ConsumerRecord<String, Posts> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        Posts postCreatedEvent = message.value();
        assertEquals(OperationKafka.CREATE, postCreatedEvent.getOperation());
        assertEquals(createProductDto.getPostContent(), postCreatedEvent.getPosts().get(0).getPostContent());
        assertEquals(createProductDto.getNumberOfLikes(), postCreatedEvent.getPosts().get(0).getNumberOfLikes());
        assertEquals(createProductDto.getTopicId(), postCreatedEvent.getPosts().get(0).getTopicId());
        assertEquals(1L, postCreatedEvent.getPosts().get(0).getUserCreatorId());
        assertEquals(1L, postCreatedEvent.getPosts().get(0).getUserOwnerId());
    }

    @Test
    void whenGoPostWeb_shouldSendRetreiveAllListOfAllPostsIT() throws ExecutionException, InterruptedException {
        //arrange part
        String title = "Samsung";

        //act block
        productService.listPost(1, 1000);

        //assert block
        ConsumerRecord<String, Posts> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        Posts postGetAllEvent = message.value();
        assertEquals(OperationKafka.RETREIVE_ALL, postGetAllEvent.getOperation());
        assertEquals(1, postGetAllEvent.getPage());
        assertEquals(1000, postGetAllEvent.getNumberPerPage());


    }

    @Test
    void whenGoPostWeb_shouldSendRetreiveOneListOfAllPostsIT() throws ExecutionException, InterruptedException {
        //arrange part
        String title = "Samsung";

        //act block
        productService.getPost(UUID.fromString("ffb71828-b077-4a5d-8749-ee394dfa81e0"));

        //assert block
        ConsumerRecord<String, Posts> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        Posts postGetEvent = message.value();
        assertEquals(OperationKafka.RETREIVE_DETAILS, postGetEvent.getOperation());
        assertEquals("ffb71828-b077-4a5d-8749-ee394dfa81e0", postGetEvent.getPosts().get(0).getPostId().toString());
    }

    @Test
    void whenGoPostWeb_shouldSendPutPostsIT() throws ExecutionException, InterruptedException {
        //arrange part
        String title = "Samsung";
        PostWebDto updatePostDto = new PostWebDto();
        updatePostDto.setPostContent(title);
        updatePostDto.setNumberOfLikes(2L);
        updatePostDto.setTopicId(12345L);
        updatePostDto.setUserCreatorId(100L);
        updatePostDto.setUserOwnerId(111L);
        Post createPost = new Post();
        //act block
        productService.updatePost(UUID.fromString("ffb71828-b077-4a5d-8749-ee394dfa81e0"),
                updatePostDto, 1L);

        //assert block
        ConsumerRecord<String, Posts> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        Posts productUpdateEvent = message.value();
        assertEquals(OperationKafka.UPDATE, productUpdateEvent.getOperation());
        assertEquals(1L, productUpdateEvent.getHeaderUserId());
        assertEquals("ffb71828-b077-4a5d-8749-ee394dfa81e0",
                productUpdateEvent.getPosts().get(0).getPostId().toString());
        assertEquals(updatePostDto.getPostContent(), productUpdateEvent.getPosts().get(0).getPostContent());
        assertEquals(updatePostDto.getNumberOfLikes(), productUpdateEvent.getPosts().get(0).getNumberOfLikes());
    }

    @Test
    void whenGoPostWeb_shouldSendDeletePostsIT() throws ExecutionException, InterruptedException {
        //arrange part

        //act block
        productService.deletePost(UUID.fromString("ffb71828-b077-4a5d-8749-ee394dfa81e0"), 1L);

        //assert block
        ConsumerRecord<String, Posts> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        Posts postDeleteEvent = message.value();
        assertEquals(OperationKafka.DELETE, postDeleteEvent.getOperation());
        assertEquals(1L, postDeleteEvent.getHeaderUserId());
        assertEquals("ffb71828-b077-4a5d-8749-ee394dfa81e0",
                postDeleteEvent.getPosts().get(0).getPostId().toString());
    }

    private Map<String, Object> getConsumerProperties() {
        Map<String, Object> config = new HashMap<>();
        System.out.println("brockers as String" + embeddedKafkaBroker.getBrokersAsString());
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        config.put(JsonDeserializer.TRUSTED_PACKAGES,
                environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"));
        config.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.group-id"));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                environment.getProperty("spring.kafka.consumer.auto-offset-reset"));
        return config;
    }
}
