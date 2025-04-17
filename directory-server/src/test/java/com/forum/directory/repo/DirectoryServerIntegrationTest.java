package com.forum.directory.repo;

import com.forum.directory.kafka.event.Directories;
import com.forum.directory.kafka.event.Directory;
import com.forum.directory.repo.kafka.listener.DirectoryListener;
import com.forum.directory.repo.repository.DirectoryPagingRepository;
import com.forum.directory.repo.repository.DirectoryRepository;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DirtiesContext //https://github.com/spring-projects/spring-kafka/issues/3225
@ContextConfiguration(initializers = {DirectoryServerIntegrationTest.Initializer.class})
@TestPropertySource(properties = {"spring.config.location=classpath:application-properties.yml"})
@Testcontainers
@EmbeddedKafka(partitions = 2, count = 1, controlledShutdown = true, topics = {"${kafka.topic.product.request}", "${kafka.topic.product.reply}"})
@SpringBootTest(properties = "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        classes = {KafkaConfigForDirectoryServer.class, DirectoryPagingRepository.class})//,KafkaConsumer.class})
@ComponentScan(basePackages = {"com.forum.directory.repo.repository"})
public class DirectoryServerIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.3")
            .withUsername("forum")
            .withPassword("ForumPassword")
            .withReuse(true)
            .withDatabaseName("directoriesdb");

    @MockBean
    DirectoryRepository processedEventRepository;
    @MockBean
    RestTemplate restTemplate;
    @Autowired
    KafkaTemplate<String, Directories> kafkaTemplate;
//    @SpyBean
//    DirectoryListener productCreatedEventHandler;


    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "CONTAINER.USERNAME=" + postgreSQLContainer.getUsername(),
                    "CONTAINER.PASSWORD=" + postgreSQLContainer.getPassword(),
                    "CONTAINER.URL=" + postgreSQLContainer.getJdbcUrl()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
    }

    @AfterAll
    static void afterAll() {
        postgreSQLContainer.stop();
    }
@Disabled
    @Test
    public void shouldReturnValidDataFromDirectoryRepository_DirectoryListenerCreate() throws ExecutionException, InterruptedException {
        //arrange
        Directory directory = new Directory();
        directory.setName("Directory about books");
        directory.setOrder(0L);
        directory.setTopicId(1L);
        List<Directory> directoryList = new ArrayList<>();
        directoryList.add(directory);
        Directories directories = new Directories();
        directories.setDirectories(directoryList);


        ProducerRecord<String, Directories> record = new ProducerRecord<>(
                "directory-req-topic",
                directories);

        //// believe next lines usless in my case
        String responseBody = "{\"name\":\"value of name\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, headers, HttpStatus.OK);

        when(restTemplate.exchange(
                any(String.class),
                any(HttpMethod.class),
                isNull(), eq(String.class)
        )).thenReturn(responseEntity);

        // act
        kafkaTemplate.send(record).get();

        //assert
        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ConsumerRecord> eventCaptor = ArgumentCaptor.forClass(ConsumerRecord.class);

//        verify(productCreatedEventHandler, timeout(10000).times(1)).listenConsumerRecord(eventCaptor.capture());
        assertEquals(directories.getDirectories().get(0).getName(),
                ((Directories) eventCaptor.getValue().value()).getDirectories().get(0).getName());
    }

}