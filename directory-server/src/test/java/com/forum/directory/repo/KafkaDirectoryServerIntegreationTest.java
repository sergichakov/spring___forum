package com.forum.directory.repo;

import com.forum.directory.kafka.event.Directories;
import com.forum.directory.kafka.event.Directory;
import com.forum.directory.kafka.event.OperationDirectoryKafka;
import com.forum.directory.repo.model.DirectoryThemeEntity;
import com.forum.directory.repo.repository.DirectoryRepository;
import com.forum.kafka.request_reply_util.CompletableFutureReplyingKafkaOperations;
//import com.forum.topic.kafka.event.OperationKafka;
//import com.forum.topic.kafka.event.Directory;
//import com.forum.topic.kafka.event.Directories;
//import com.forum.topic.repo.model.DirectoryThemeEntity;
//import com.forum.topic.repo.repository.DirectoryRepository;
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
@EmbeddedKafka(partitions = 2, count = 1, controlledShutdown = true, topics = {"${kafka.topic.product.request}", "${kafka.topic.product.reply}"})
// partitions=1
@SpringBootTest(properties = "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}")

public class KafkaDirectoryServerIntegreationTest {
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.3")
            .withUsername("forum")
            .withPassword("ForumPassword")
            .withReuse(true)
            .withDatabaseName("directoriesdb");
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private CompletableFutureReplyingKafkaOperations<String, Directories, Directories> replyKafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private DirectoryRepository directoryRepository;
    @Value("${kafka.topic.product.request}")
    private String requestDirectory;

    @Value("${kafka.topic.product.reply}")
    private String requestReplyDirectory;

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
    public void setupDirectoriesRepository() {
        for (DirectoryThemeEntity topicEntityBefore : lookUpFindAll()) {
            directoryRepository.delete(topicEntityBefore);
        }
        DirectoryThemeEntity topicEntity = new DirectoryThemeEntity();
        topicEntity.setName("Label of topic");
        topicEntity.setTopicId(333L);
        topicEntity.setSubDirId(111L);
        directoryRepository.save(topicEntity);
    }

    @Test
    public void shouldReturnDirectoryThemeEntity_whenRetrieveAllDataFromKafka() throws Exception {
        Directories directoriesRequest = new Directories();
        directoriesRequest.setPage(0);
        directoriesRequest.setNumberPerPage(1000);
        directoriesRequest.setOperation(OperationDirectoryKafka.RETREIVE_ALL);
        Directories actualDirectories = replyKafkaTemplate.requestReply(requestDirectory, directoriesRequest).get();

        Directory oneDirectory = actualDirectories.getDirectories().get(0);

        assertEquals("Label of topic", oneDirectory.getName());
        assertEquals(333L, oneDirectory.getTopicId());
        assertEquals(111L, oneDirectory.getSubDirId());
    }

    @Test
    public void shouldReturnOneDirectoryThemeEntity_whenRetrieveOneDataFromKafka() throws Exception {
        DirectoryThemeEntity directoryInitialEntity = lookUpFindAll().get(0);
        Long directoryId = directoryInitialEntity.getDirectoryId();
        Directory directoryInside = new Directory();

        directoryInside.setDirectoryId(directoryId);
        Directories directoriesRequest = new Directories();
        directoriesRequest.setPage(0);
        directoriesRequest.setNumberPerPage(1000);
        directoriesRequest.setOperation(OperationDirectoryKafka.RETREIVE_DETAILS);
        directoriesRequest.setDirectories(List.of(directoryInside));
        //Action
        Directories actualDirectories = replyKafkaTemplate.requestReply(requestDirectory, directoriesRequest).get();


        Directory oneDirectory = actualDirectories.getDirectories().get(0);
        assertEquals(directoryId, oneDirectory.getDirectoryId());
        assertEquals("Label of topic", oneDirectory.getName());
        assertEquals(333L, oneDirectory.getTopicId());
        assertEquals(111L, oneDirectory.getSubDirId());
    }

    @Test
    @Commit
    public void shouldUpdateEntityInDb_whenEditData() throws Exception {
        //arrange
        DirectoryThemeEntity topicInitialEntity = lookUpFindAll().get(0);
        Long directoryInitialPostId = topicInitialEntity.getDirectoryId();
        Directory directory = new Directory();
        directory.setDirectoryId(directoryInitialPostId);
        directory.setName("Label of changes in original");
        directory.setSubDirId(111L);
        Directories directoriesRequestEdition = new Directories();
        directoriesRequestEdition.setPage(0);
        directoriesRequestEdition.setNumberPerPage(1000);
        directoriesRequestEdition.setOperation(OperationDirectoryKafka.UPDATE);
        directoriesRequestEdition.setDirectories(List.of(directory));
        //act
        Directories actualDirectories = replyKafkaTemplate.requestReply(requestDirectory, directoriesRequestEdition).get();

        System.out.println("Operation of " + actualDirectories.getOperation());

        DirectoryThemeEntity directoryEntity = lookUpFindAll().get(0);
        assertEquals(OperationDirectoryKafka.SUCCESS, actualDirectories.getOperation());
        assertEquals("Label of changes in original", directoryEntity.getName());
        assertEquals(111L, directoryEntity.getSubDirId());
        assertEquals(directoryInitialPostId, directoryEntity.getDirectoryId());
    }

    @Transactional
    @Commit
    public List<DirectoryThemeEntity> lookUpFindAll() {
        List<DirectoryThemeEntity> listDirectory = new ArrayList<>();

        for (DirectoryThemeEntity directoryEntity : directoryRepository.findAll()) {
            listDirectory.add(directoryEntity);
        }
        return listDirectory;
    }

    @Test
    @Commit
    public void shouldCreateEntityInDb_whenCreateNewData() throws Exception {
        DirectoryThemeEntity directoryEntityInitial = lookUpFindAll().get(0);
        directoryRepository.delete(directoryEntityInitial);
        //arrange
        Directory direcotory = new Directory();
        direcotory.setName("Label new DirectoryThemeEntity");
        direcotory.setSubDirId(222L);
        Directories directoriesRequestEdition = new Directories();
        directoriesRequestEdition.setPage(0);
        directoriesRequestEdition.setNumberPerPage(1000);
        directoriesRequestEdition.setOperation(OperationDirectoryKafka.CREATE);
        directoriesRequestEdition.setDirectories(List.of(direcotory));
        //act
        Directories actualDirectories = replyKafkaTemplate.requestReply(requestDirectory, directoriesRequestEdition).get();

        System.out.println("Operation of " + actualDirectories.getOperation());

        assertEquals(OperationDirectoryKafka.SUCCESS, actualDirectories.getOperation());
        DirectoryThemeEntity topicEntity = lookUpFindAll().get(0);
        assertEquals("Label new DirectoryThemeEntity", topicEntity.getName());
        assertEquals(222L, topicEntity.getSubDirId());
    }

    @Test
    @Commit
    public void shouldDeleteEntityInDb_whenDeleteData() throws Exception {
        //arrange
        DirectoryThemeEntity directoryInitialEntity = lookUpFindAll().get(0);
        Long directoryInitialPostId = directoryInitialEntity.getDirectoryId();
        Directory directory = new Directory();
        directory.setDirectoryId(directoryInitialPostId);
        directory.setSubDirId(111L);
        Directories directoriesRequestEdition = new Directories();
        directoriesRequestEdition.setPage(0);
        directoriesRequestEdition.setNumberPerPage(1000);
        directoriesRequestEdition.setOperation(OperationDirectoryKafka.DELETE);
        directoriesRequestEdition.setDirectories(List.of(directory));
        //act
        Directories actualDirectories = replyKafkaTemplate.requestReply(requestDirectory, directoriesRequestEdition).get();

        System.out.println("Operation of " + actualDirectories.getOperation());

        assertEquals(OperationDirectoryKafka.SUCCESS, actualDirectories.getOperation());
    }
}
