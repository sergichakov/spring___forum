package com.forum.topic.web.controller;

import com.forum.jwk.fetch.JwtKeyComponent;
import com.forum.jwk.service.JwtService;
import com.forum.topic.kafka.event.OperationKafka;
import com.forum.topic.kafka.event.Topic;
import com.forum.topic.kafka.event.Topics;
import com.forum.topic.kafka.event.UserDetailsRole;
import com.forum.topic.web.model.TopicWebDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

//@RunWith(SpringRunner.class) //ProductMicroserviceApplication.class
//@Disabled
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test") ////было так partitions = 5
@EmbeddedKafka(partitions = 1, count = 1, controlledShutdown = true)
//, kraft=true)////,brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
//@EmbeddedKafka(partitions = 3, count=1, controlledShutdown = true)

@SpringBootTest(properties = "spring.kafka.producer.bootstrap-servers-${spring.embedded.kafka.brokers}")
////,classes = ProductServiceImpl.class)
public class TopicWebControllerIntegrationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicWebControllerIntegrationTest.class);

    @Autowired
    private Environment environment;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private com.forum.topic.web.service.TopicWebService productService;
    private KafkaMessageListenerContainer<String, Topics> container;
    private BlockingQueue<ConsumerRecord<String, Topics>> records;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private JwtKeyComponent jwtKeyComponent;
    @BeforeAll
    void setUp() throws ExecutionException, InterruptedException {
        Mockito.when(jwtService.isTokenValid(any())).thenReturn(true);
        DefaultKafkaConsumerFactory<String, Object> consumerFactory =
                new DefaultKafkaConsumerFactory<>(getConsumerProperties());
        ContainerProperties containerProperties =
                new ContainerProperties(environment.getProperty("product-created-events-topic-name"));
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, Topics>) records::add);
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
    void whenGoTopicsWeb_shouldSendCreatedPostIT() throws ExecutionException, InterruptedException {
        //arrange part
        String title = "Samsung";
        TopicWebDto createProductDto = new TopicWebDto();
        createProductDto.setPostContent(title);
        createProductDto.setDirectoryId(2L);
        createProductDto.setTopicLabel("Text about Topic");
        createProductDto.setUserOwnerId(111L);

        //act block
        productService.createTopic(createProductDto);

        //assert block
        ConsumerRecord<String, Topics> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        Topics postCreatedEvent = message.value();
        assertEquals(OperationKafka.CREATE, postCreatedEvent.getOperation());
        assertEquals(createProductDto.getPostContent(), postCreatedEvent.getTopics().get(0).getPostContent());
        assertEquals(createProductDto.getDirectoryId(), postCreatedEvent.getTopics().get(0).getDirectoryId());
        assertEquals(createProductDto.getTopicLabel(), postCreatedEvent.getTopics().get(0).getTopicLabel());
        assertEquals(createProductDto.getUserOwnerId(), postCreatedEvent.getTopics().get(0).getUserOwnerId());
    }

    @Test
    void whenGoDirectoriesWeb_shouldSendRetreiveAllListOfAllPostsIT() throws ExecutionException, InterruptedException {
        //arrange part
        String title = "Samsung";

        //act block
        productService.listTopic(1, 1000, 1L, UserDetailsRole.ROLE_USER);

        //assert block
        ConsumerRecord<String, Topics> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        Topics postGetAllEvent = message.value();
        assertEquals(OperationKafka.RETREIVE_ALL, postGetAllEvent.getOperation());
        assertEquals(1, postGetAllEvent.getPage());
        assertEquals(1000, postGetAllEvent.getNumberPerPage());
    }

    @Test
    void whenGoTopicsWeb_shouldSendRetreiveOneListOfAllPostsIT() throws ExecutionException, InterruptedException {
        //arrange part
        String title = "Samsung";
        //act block
        productService.getTopic(500L, 1L, UserDetailsRole.ROLE_USER);

        //assert block
        ConsumerRecord<String, Topics> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        Topics postGetEvent = message.value();
        assertEquals(OperationKafka.RETREIVE_DETAILS, postGetEvent.getOperation());

        assertEquals(500L, postGetEvent.getTopics().get(0).getPostId());
    }

    @Test
    void whenGoTopicsWeb_shouldSendPutPostsIT() throws ExecutionException, InterruptedException {
        //arrange part
        String title = "Samsung";
        TopicWebDto updatePostDto = new TopicWebDto();
        updatePostDto.setPostContent(title); //.setName(title);
        updatePostDto.setDirectoryId(2L);//.setOrder(0L);
        updatePostDto.setTopicLabel("12345L");
        updatePostDto.setUserOwnerId(111L);
        //act block
        productService.updatePost(500L, updatePostDto, 1L, UserDetailsRole.ROLE_USER);

        //assert block
        ConsumerRecord<String, Topics> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        Topics productUpdateEvent = message.value();
        assertEquals(OperationKafka.UPDATE, productUpdateEvent.getOperation());
        assertEquals(1L, productUpdateEvent.getHeaderUserId());
        assertEquals(500L, productUpdateEvent.getTopics().get(0).getPostId());//.toString());
        assertEquals(updatePostDto.getPostContent(), productUpdateEvent.getTopics().get(0).getPostContent());
        assertEquals(updatePostDto.getPostContent(), productUpdateEvent.getTopics().get(0).getPostContent());
        assertEquals(updatePostDto.getTopicLabel(), productUpdateEvent.getTopics().get(0).getTopicLabel());
        assertEquals(updatePostDto.getUserOwnerId(), productUpdateEvent.getTopics().get(0).getUserOwnerId());
        assertEquals(updatePostDto.getUserOwnerId(), productUpdateEvent.getTopics().get(0).getUserOwnerId());
    }

    @Test
    void whenGoTopicsWeb_shouldSendDeletePostsIT() throws ExecutionException, InterruptedException {
        //arrange part

        //act block
        productService.deletePost(200L, 1L, UserDetailsRole.ROLE_USER);

        //assert block
        ConsumerRecord<String, Topics> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        Topics postDeleteEvent = message.value();
        assertEquals(OperationKafka.DELETE, postDeleteEvent.getOperation());
        assertEquals(1L, postDeleteEvent.getHeaderUserId());
        assertEquals("ffb71828-b077-4a5d-8749-ee394dfa81e0",
                postDeleteEvent.getTopics().get(0).getPostId().toString());
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
