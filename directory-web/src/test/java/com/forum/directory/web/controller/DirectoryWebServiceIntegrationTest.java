package com.forum.directory.web.controller;

import com.forum.directory.kafka.event.Directories;
import com.forum.directory.web.model.DirectoryWebDto;
import com.forum.directory.web.service.DirectoryWebService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, count = 1, controlledShutdown = true)
@SpringBootTest(properties = "spring.kafka.producer.bootstrap-servers-${spring.embedded.kafka.brokers}")
////,classes = ProductServiceImpl.class)
public class DirectoryWebServiceIntegrationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryWebServiceIntegrationTest.class);

    @Autowired
    private Environment environment;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private DirectoryWebService productService;
    private KafkaMessageListenerContainer<String, Directories> container;
    private BlockingQueue<ConsumerRecord<String, Directories>> records;

    @BeforeAll
    void setUp() {
        DefaultKafkaConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(getConsumerProperties());
        ContainerProperties containerProperties = new ContainerProperties(environment.getProperty("product-created-events-topic-name"));

        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, Directories>) records::add);
        container.start();
        LOGGER.info(" container just started");
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @AfterAll
    void tearDown() {
        container.stop();
    }

    @Test
    void whenGoDirectoriesWeb_shouldGetAllDirectoriesIT() throws ExecutionException, InterruptedException {
        //arrange part

        //act block
        productService.listDirectory(1, 1000);

        //assert block
        ConsumerRecord<String, Directories> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        Directories productCreatedEvent = message.value();
        assertEquals(1, productCreatedEvent.getPage());
        assertEquals(1000, productCreatedEvent.getNumberPerPage());

    }

    @Test
    void whenGoDirectoriesWeb_shouldGetOneDirectoryIT() throws ExecutionException, InterruptedException {
        //arrange part

        //act block
        productService.getDirectory(2L);

        //assert block
        ConsumerRecord<String, Directories> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        Directories productCreatedEvent = message.value();
        assertEquals(2L, productCreatedEvent.getDirectories().get(0).getDirectoryId());


    }

    @Test
    void whenGoDirectoriesWeb_shouldSendCreateDirectoriesIT() throws ExecutionException, InterruptedException {
        //arrange part
        String title = "Samsung";
        DirectoryWebDto createProductDto = new DirectoryWebDto();
        createProductDto.setName(title);
        createProductDto.setOrder(0L);
        createProductDto.setTopicId(1L);

        //act block
        productService.createDirectory(createProductDto);

        //assert block
        ConsumerRecord<String, Directories> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        Directories productCreatedEvent = message.value();
        assertEquals(createProductDto.getName(), productCreatedEvent.getDirectories().get(0).getName());//getQuantity(),productCreatedEvent.getQuantity());
        assertEquals(createProductDto.getOrder(), productCreatedEvent.getDirectories().get(0).getOrder());
        assertEquals(createProductDto.getTopicId(), productCreatedEvent.getDirectories().get(0).getTopicId());
    }

    @Test
    void whenGoDirectoriesWeb_shouldSendUpdateDirectoriesIT() throws ExecutionException, InterruptedException {
        //arrange part
        String title = "Samsung";
        DirectoryWebDto createProductDto = new DirectoryWebDto();
        createProductDto.setName(title);
        createProductDto.setOrder(0L);
        createProductDto.setTopicId(1L);

        //act block
        productService.updateDirectory(1L, createProductDto);

        //assert block
        ConsumerRecord<String, Directories> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        Directories productCreatedEvent = message.value();
        assertEquals(1L, productCreatedEvent.getDirectories().get(0).getDirectoryId());
        assertEquals(createProductDto.getName(), productCreatedEvent.getDirectories().get(0).getName());
        assertEquals(createProductDto.getOrder(), productCreatedEvent.getDirectories().get(0).getOrder());
        assertEquals(createProductDto.getTopicId(), productCreatedEvent.getDirectories().get(0).getTopicId());
    }

    @Test
    void whenGoDirectoriesWeb_shouldSendDeleteDirectoriesIT() throws ExecutionException, InterruptedException {
        //act block
        productService.deleteDirectory(1L); //.createProduct(createProductDto);

        //assert block
        ConsumerRecord<String, Directories> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        //assertNotNull(message.key()); //// потому что у Индуса нет ключа в message
        Directories productCreatedEvent = message.value();
        assertEquals(1L, productCreatedEvent.getDirectories().get(0).getDirectoryId());
    }

    private Map<String, Object> getConsumerProperties() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        config.put(JsonDeserializer.TRUSTED_PACKAGES, environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"));
        config.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.group-id"));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, environment.getProperty("spring.kafka.consumer.auto-offset-reset"));
        return config;
    }
}
