package com.forum.topic.web.controller;

import com.forum.topic.kafka.event.OperationKafka;
import com.forum.topic.kafka.event.Topic;
import com.forum.topic.kafka.event.Topics;
import com.forum.topic.kafka.event.UserDetailsRole;
import com.forum.topic.web.model.TopicWebDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

//@RunWith(SpringRunner.class) //ProductMicroserviceApplication.class
//@Disabled
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test") ////было так partitions = 5
@EmbeddedKafka(partitions=1, count=1, controlledShutdown = true)//, kraft=true)////,brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
//@EmbeddedKafka(partitions = 3, count=1, controlledShutdown = true)
// killthis delete this clause line brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" }
@SpringBootTest(properties="spring.kafka.producer.bootstrap-servers-${spring.embedded.kafka.brokers}")////,classes = ProductServiceImpl.class)
public class TopicWebControllerIntegrationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicWebControllerIntegrationTest.class);

    @Autowired
    private Environment environment;
//    @Autowired
//    private ProductService productService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private com.forum.topic.web.service.TopicWebService productService;
    private KafkaMessageListenerContainer<String, Topics> container;
    private BlockingQueue<ConsumerRecord<String, Topics>> records;
    @BeforeAll
    void setUp(){
        DefaultKafkaConsumerFactory<String,Object> consumerFactory=new DefaultKafkaConsumerFactory<>(getConsumerProperties());
        ContainerProperties containerProperties = new ContainerProperties(environment.getProperty("product-created-events-topic-name"));//,"post-req-reply-topic");
///////////////////////////////////////////////////
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, Topics>) records::add);
        container.start();

        LOGGER.info(" container just started");
        ContainerTestUtils.waitForAssignment(container,embeddedKafkaBroker.getPartitionsPerTopic());
    }
    @AfterAll
    void tearDown(){
        container.stop();
    }

//@Disabled
    @Test
    void whenGoTopicsWeb_shouldSendCreatedPostIT() throws ExecutionException, InterruptedException {
        //arrange part
        String title="Samsung";
        //BigDecimal price=new BigDecimal(600);
        //Integer quantity=1;
        TopicWebDto createProductDto=new TopicWebDto();
        createProductDto.setPostContent(title); //.setName(title);
        createProductDto.setDirectoryId(2L);//.setOrder(0L);
        createProductDto.setTopicLabel("Text about Topic");
        createProductDto.setUserOwnerId(111L);

        //act block
        productService.createTopic(createProductDto); //.createProduct(createProductDto);

        //assert block
        ConsumerRecord<String, Topics> message= records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        //assertNotNull(message.key()); //// потому что у Индуса нет ключа в message
        Topics postCreatedEvent = message.value();
        assertEquals(OperationKafka.CREATE,postCreatedEvent.getOperation());
        assertEquals(createProductDto.getPostContent(),postCreatedEvent.getTopics().get(0).getPostContent());//getQuantity(),productCreatedEvent.getQuantity());
        assertEquals(createProductDto.getDirectoryId(),postCreatedEvent.getTopics().get(0).getDirectoryId());
        assertEquals(createProductDto.getTopicLabel(),postCreatedEvent.getTopics().get(0).getTopicLabel());
        assertEquals(createProductDto.getUserOwnerId(),postCreatedEvent.getTopics().get(0).getUserOwnerId());
    }

    @Test
    void whenGoDirectoriesWeb_shouldSendRetreiveAllListOfAllPostsIT() throws ExecutionException, InterruptedException {
        //arrange part
        String title="Samsung";
        //BigDecimal price=new BigDecimal(600);
        //Integer quantity=1;
//        PostWebDto createProductDto=new PostWebDto();
//        createProductDto.setPostContent(title); //.setName(title);
//        createProductDto.setNumberOfLikes(2L);//.setOrder(0L);
//        createProductDto.setTopicId(12345L);
//        createProductDto.setUserCreatorId(100L);
//        createProductDto.setUserOwnerId(111L);

        //act block
        productService.listTopic(1,1000,1L, UserDetailsRole.ROLE_USER);//.createPost(createProductDto); //.createProduct(createProductDto);

        //assert block
        ConsumerRecord<String, Topics> message= records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        //assertNotNull(message.key()); //// потому что у Индуса нет ключа в message
        Topics postGetAllEvent = message.value();
        assertEquals(OperationKafka.RETREIVE_ALL, postGetAllEvent.getOperation());
        assertEquals(1,postGetAllEvent.getPage());
        assertEquals(1000,postGetAllEvent.getNumberPerPage());
//        assertEquals(createProductDto.getPostContent(),productCreatedEvent.getPosts().get(0).getPostContent());//getQuantity(),productCreatedEvent.getQuantity());
//        assertEquals(createProductDto.getNumberOfLikes(),productCreatedEvent.getPosts().get(0).getNumberOfLikes());
//        assertEquals(createProductDto.getTopicId(),productCreatedEvent.getPosts().get(0).getTopicId());
//        assertEquals(createProductDto.getUserCreatorId(),productCreatedEvent.getPosts().get(0).getUserCreatorId());
//        assertEquals(createProductDto.getUserOwnerId(),productCreatedEvent.getPosts().get(0).getUserOwnerId());
    }
    @Test
    void whenGoTopicsWeb_shouldSendRetreiveOneListOfAllPostsIT() throws ExecutionException, InterruptedException {
        //arrange part
        String title="Samsung";
        //BigDecimal price=new BigDecimal(600);
        //Integer quantity=1;
//        PostWebDto createProductDto=new PostWebDto();
//        createProductDto.setPostContent(title); //.setName(title);
//        createProductDto.setNumberOfLikes(2L);//.setOrder(0L);
//        createProductDto.setTopicId(12345L);
//        createProductDto.setUserCreatorId(100L);
//        createProductDto.setUserOwnerId(111L);

        //act block
        productService.getTopic(500L, 1L, UserDetailsRole.ROLE_USER);//.createPost(createProductDto); //.createProduct(createProductDto);

        //assert block
        ConsumerRecord<String, Topics> message= records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        //assertNotNull(message.key()); //// потому что у Индуса нет ключа в message
        Topics postGetEvent = message.value();
        assertEquals(OperationKafka.RETREIVE_DETAILS, postGetEvent.getOperation());
//        assertEquals(1,productCreatedEvent.getPage());
//        assertEquals(1000,productCreatedEvent.getNumberPerPage());
        assertEquals(500L, postGetEvent.getTopics().get(0).getPostId());//getQuantity(),productCreatedEvent.getQuantity());
//        assertEquals(createProductDto.getNumberOfLikes(),productCreatedEvent.getPosts().get(0).getNumberOfLikes());
//        assertEquals(createProductDto.getTopicId(),productCreatedEvent.getPosts().get(0).getTopicId());
//        assertEquals(createProductDto.getUserCreatorId(),productCreatedEvent.getPosts().get(0).getUserCreatorId());
//        assertEquals(createProductDto.getUserOwnerId(),productCreatedEvent.getPosts().get(0).getUserOwnerId());
    }
    @Test
    void whenGoTopicsWeb_shouldSendPutPostsIT() throws ExecutionException, InterruptedException {
        //arrange part
        String title="Samsung";
        //BigDecimal price=new BigDecimal(600);
        //Integer quantity=1;
        TopicWebDto updatePostDto=new TopicWebDto();
        updatePostDto.setPostContent(title); //.setName(title);
        updatePostDto.setDirectoryId(2L);//.setOrder(0L);
        updatePostDto.setTopicLabel("12345L");
        updatePostDto.setUserOwnerId(111L);
        Topic createTopic = new Topic();
        //act block
        productService.updatePost(500L,updatePostDto,1L, UserDetailsRole.ROLE_USER);//.createPost(createProductDto); //.createProduct(createProductDto);

        //assert block
        ConsumerRecord<String, Topics> message= records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        //assertNotNull(message.key()); //// потому что у Индуса нет ключа в message
        Topics productUpdateEvent = message.value();
        assertEquals(OperationKafka.UPDATE, productUpdateEvent.getOperation());
        assertEquals(1L,productUpdateEvent.getHeaderUserId());
        assertEquals(500L, productUpdateEvent.getTopics().get(0).getPostId().toString());
        assertEquals(updatePostDto.getPostContent(),productUpdateEvent.getTopics().get(0).getPostContent());//getQuantity(),productCreatedEvent.getQuantity());
        assertEquals(updatePostDto.getPostContent(),productUpdateEvent.getTopics().get(0).getPostContent());
        assertEquals(updatePostDto.getTopicLabel(),productUpdateEvent.getTopics().get(0).getTopicLabel());
        assertEquals(updatePostDto.getUserOwnerId(),productUpdateEvent.getTopics().get(0).getUserOwnerId());
        assertEquals(updatePostDto.getUserOwnerId(),productUpdateEvent.getTopics().get(0).getUserOwnerId());
    }
    @Test
    void whenGoTopicsWeb_shouldSendDeletePostsIT() throws ExecutionException, InterruptedException {
        //arrange part

        //act block
        productService.deletePost(200L,1L, UserDetailsRole.ROLE_USER);//.createPost(createProductDto); //.createProduct(createProductDto);

        //assert block
        ConsumerRecord<String, Topics> message= records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        //assertNotNull(message.key()); //// потому что у Индуса нет ключа в message
        Topics postDeleteEvent = message.value();
        assertEquals(OperationKafka.DELETE, postDeleteEvent.getOperation());
        assertEquals(200L,postDeleteEvent.getHeaderUserId());
        assertEquals("ffb71828-b077-4a5d-8749-ee394dfa81e0", postDeleteEvent.getTopics().get(0).getPostId().toString());
    }
    private Map<String,Object> getConsumerProperties() {
        Map<String,Object> config=new HashMap<>();
        System.out.println("brockers as String"+embeddedKafkaBroker.getBrokersAsString());
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,embeddedKafkaBroker.getBrokersAsString());//                 getProperty("spring.kafka.consumer.bootstrap-servers"));
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);//environment.getProperty("org.apache.kafka.common.serialization.StringDeserializer"));
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);//environment.getProperty("org.springframework.kafka.support.serializer.JsonDeserializer"));
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        config.put(JsonDeserializer.TRUSTED_PACKAGES, environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"));
        config.put(ConsumerConfig.GROUP_ID_CONFIG,environment.getProperty("spring.kafka.consumer.group-id"));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,environment.getProperty("spring.kafka.consumer.auto-offset-reset"));
        return config;
    }
}
