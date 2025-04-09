package com.forum.post.repo;

import com.forum.post.kafka.event.Posts;
import com.forum.post.kafka.event.Post;
import com.forum.post.repo.kafka.event.PostListener;
import com.forum.post.repo.repository.PostPagingRepository;
import com.forum.post.repo.repository.PostRepository;

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
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

//@DataJpaTest(properties={"spring.liquibase.enabled=false"}) //multiple BootStrapWith()
@DirtiesContext //https://github.com/spring-projects/spring-kafka/issues/3225
@ContextConfiguration(initializers = {PostServerIntegrationTest.Initializer.class})
@TestPropertySource(properties = {"spring.config.location=classpath:application-properties.yml"})
@Testcontainers
//@EmbeddedKafka
@SpringBootTest(//properties = "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        classes = {KafkaConfigForDirectoryServer.class, PostPagingRepository.class})//,KafkaConsumer.class})
@ComponentScan(basePackages = {"com.forum.post.repo.repository"})
public class PostServerIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.3")
            .withUsername("jira")
            .withPassword("JiraRush")
            .withReuse(true)
            .withDatabaseName("productdb");

    @MockBean
    PostRepository processedEventRepository;
    @MockBean
    RestTemplate restTemplate;
    @Autowired
    KafkaTemplate<String, Posts> kafkaTemplate;
    @SpyBean
    PostListener productCreatedEventHandler;


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
    public void shouldReturnValidDataFromDirectoryRepository_DirectoryListenerCreate()
            throws ExecutionException, InterruptedException {
        //arrange
        Post post = new Post();
        post.setPostContent("Directory about books");
        post.setNumberOfLikes(0L);
        UUID postUUID = UUID.randomUUID();
        post.setPostId(postUUID);
        List<Post> postList = new ArrayList<>();
        postList.add(post);
        Posts posts = new Posts();
        posts.setPosts(postList);


        ProducerRecord<String, Posts> record = new ProducerRecord<>(
                "post-req-topic",
                posts);

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

        verify(productCreatedEventHandler, timeout(10000).times(1)).listenConsumerRecord(eventCaptor.capture());

        assertEquals(posts.getPosts().get(0).getPostContent(),
                ((Posts) eventCaptor.getValue().value()).getPosts().get(0).getPostContent());
    }

}