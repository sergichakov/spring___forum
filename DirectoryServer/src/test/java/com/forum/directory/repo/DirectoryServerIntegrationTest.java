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
//@DataJpaTest(properties={"spring.liquibase.enabled=false"}) //multiple BootStrapWith()
@DirtiesContext //https://github.com/spring-projects/spring-kafka/issues/3225
@ContextConfiguration(initializers = {DirectoryServerIntegrationTest.Initializer.class})
@TestPropertySource(properties = {"spring.config.location=classpath:application-properties.yml"})
@Testcontainers
//@EmbeddedKafka
@SpringBootTest(//properties = "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        classes={KafkaConfigForDirectoryServer.class, DirectoryPagingRepository.class})//,KafkaConsumer.class})
@ComponentScan(basePackages={"com.forum.directory.repo.repository"})
public class DirectoryServerIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.3")
            .withUsername("jira")
            .withPassword("JiraRush")
            .withReuse(true)
            .withDatabaseName("productdb");

    @MockBean
    DirectoryRepository processedEventRepository;
    @MockBean
    RestTemplate restTemplate;
    @Autowired
    KafkaTemplate<String,Directories> kafkaTemplate;
    @SpyBean
    DirectoryListener productCreatedEventHandler;
//    @Autowired
//    KafkaConsumer kafkaConsumer;


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
//        sessionObject = SessionObject.builder()
//                .name("ADMIN")
//                .password("ADMIN")
//                .build();
        postgreSQLContainer.start();
    }
    @AfterAll
    static void afterAll() {
        postgreSQLContainer.stop();
    }

    @Test
    public void shouldReturnValidDataFromDirectoryRepository_DirectoryListenerCreate() throws ExecutionException, InterruptedException {
        //arrange
        Directory directory=new Directory();
        directory.setName("Directory about books");
        directory.setOrder(0L);
        directory.setTopicId(1L);
        List<Directory> directoryList=new ArrayList<>();
        directoryList.add(directory);
        Directories directories = new Directories();
        directories.setDirectories(directoryList);
//        directories.setPrice(new BigDecimal(10));
//        directories.setProductId(UUID.randomUUID().toString());
//        directories.setQuantity(1);
//        directories.setTitle("Test product");
//        String messageId = UUID.randomUUID().toString();
//        String messageKey = directories.getProductId();

        ProducerRecord<String,Directories> record = new ProducerRecord<>( // here was <String,Object>
                "directory-req-topic",//"product-created-events-topic",
                //messageKey,
                directories);
//        record.headers().add("messageId",messageId.getBytes());
//        record.headers().add(KafkaHeaders.RECEIVED_KEY,messageKey.getBytes());
            //// believe next lines usless in my case
//        DirectoryThemeEntity processedEventEntity = new DirectoryThemeEntity();
//        List<DirectoryThemeEntity> processedEventEntityList = new ArrayList<>();
//        processedEventEntityList.add(processedEventEntity);
//        when(processedEventRepository.findByName(anyString())).thenReturn(processedEventEntityList);
//        when(processedEventRepository.save(any(DirectoryThemeEntity.class))).thenReturn(null);

        String responseBody = "{\"name\":\"value of name\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, headers, HttpStatus.OK);

        when(restTemplate.exchange(
                any(String.class),
                any(HttpMethod.class),
                isNull(), eq (String.class)
        )).thenReturn(responseEntity);

        // act
        kafkaTemplate.send(record).get();
//        boolean messageConsumed = kafkaConsumer.getLatch().await(10, TimeUnit.SECONDS);
        //assert
        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ConsumerRecord> eventCaptor = ArgumentCaptor.forClass(ConsumerRecord.class); //forClass(DirectoryThemeEntity.class);

        verify(productCreatedEventHandler , timeout(10000).times(1)). listenConsumerRecord(eventCaptor.capture());
//                messageIdCaptor.capture(),
//                messageKeyCaptor.capture());
//        eventCaptor.capture();messageIdCaptor.capture();messageKeyCaptor.capture();
//        assertEquals(messageId, messageIdCaptor.getValue());
//        assertEquals(messageKey, messageKeyCaptor.getValue());
        assertEquals(directories.getDirectories().get(0).getName(),
                ((Directories)eventCaptor.getValue().value()).getDirectories().get(0).getName());//getProductId(), eventCaptor.getValue().getProductId());
    }

}