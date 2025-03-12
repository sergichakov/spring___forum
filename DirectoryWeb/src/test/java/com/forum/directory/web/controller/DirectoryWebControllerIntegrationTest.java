package com.forum.directory.web.controller;

import com.forum.directory.kafka.event.Directories;
import com.forum.directory.kafka.event.OperationDirectoryKafka;
import com.forum.directory.web.service.DirectoryWebService;
import com.forum.directory.web.service.DirectoryWebServiceImpl;
import com.forum.kafka.request_reply_util.CompletableFutureReplyingKafkaOperations;
import com.forum.kafka.request_reply_util.CompletableFutureReplyingKafkaTemplate;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@Import(WebControllerKafkaConfiguration.class)//.WebControllerConfiguration.class)//DirectoryWebControllerIntegrationTest.WebControllerConfiguration.class)
@WebMvcTest(properties="spring.kafka.producer.bootstrap-servers-${spring.embedded.kafka.brokers}")
//@DirtiesContext
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka( partitions = 2, count=3, controlledShutdown = true,
        topics={"${kafka.topic.product.request}", "${kafka.topic.product.reply}"}, kraft=true,
        brokerProperties = { "transaction.state.log.replication.factor=3", "transaction.state.log.min.isr=1", "log.dir=/home/user/out/embedded-kafka"})

// partitions=1
//, kraft=true)////,brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
// killthis delete this clause line brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" }
//@SpringBootTest(properties="spring.kafka.producer.bootstrap-servers-${spring.embedded.kafka.brokers}")////,classes = ProductServiceImpl.class)
public class DirectoryWebControllerIntegrationTest {
    @Autowired
    public EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    MockMvc mvc;
    @Autowired
    private DirectoryWebService directoryWebService;
    @Test
    public void shouldHaveEqualFieldesWhenSend() throws Exception {
        mvc.perform(get("/directoriesweb")).andExpect(status().isOk());
    }
//    @KafkaListener(topics = "${kafka.topic.product.request}", containerFactory = "requestReplyListenerContainerFactory")
//    @SendTo
//    @TestConfiguration
    public class ActionListener{
        @KafkaListener(topics = "${kafka.topic.product.request}", containerFactory = "requestReplyListenerContainerFactory")
        @SendTo
        public Directories listenConsumerRecord(ConsumerRecord<String, Directories> record){
            Directories directoriesResult=new Directories();
            Directories directories= record.value();
            directoriesResult.setOperation(OperationDirectoryKafka.SUCCESS);
            directoriesResult.setMax(0L);
            directoriesResult.setDirectories(directories.getDirectories());
            directoriesResult.setPage(directories.getPage());
            directoriesResult.setNumberPerPage(directories.getNumberPerPage());

            return directories;
        }
    }
//
}
