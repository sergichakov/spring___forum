package com.forum.post.repo;

import com.forum.kafka.request_reply_util.CompletableFutureReplyingKafkaOperations;
import com.forum.kafka.request_reply_util.CompletableFutureReplyingKafkaTemplate;
import com.forum.post.kafka.event.Posts;
//import com.forum.topic.kafka.event.Topics;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Configuration    //now it is not a @Bean it conflicts with actual KafkaConfig
public class KafkaConfigForDirectoryServer {
    @Value("${spring.embedded.kafka.brokers}")//("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${kafka.topic.product.request}")
    private String requestTopic;
//    @Autowired
//    Map<String, Object> producerConfigs;
//    @Autowired
//    Map<String, Object> consumerConfigs;
//    @Autowired
//    ProducerFactory<String, Topics> replyProducerFactory;
//    @Autowired
//    KafkaTemplate<String, Topics> replyTemplate;

    @Value("${kafka.request-reply.timeout-ms}")
    private Long replyTimeout;
    @Value("${kafka.topic.product.reply}")
    private String replyTopic;
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    @Bean
    public KafkaTemplate<String, Posts> replyTemplate2() {
        return new KafkaTemplate<>(replyProducerFactory2());
    }
    @Bean
    public Map<String, Object> consumerConfigs2() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);//// is bootstrapServers not right - 9092
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        return props;
    }
//    @Bean
    public ProducerFactory<String, Posts> replyProducerFactory2() {
        return new DefaultKafkaProducerFactory<>(producerConfigs2());
    }
    @Bean
    public Map<String, Object> producerConfigs2() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);//// is bootstrapServers not right - 9092
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public CompletableFutureReplyingKafkaOperations<String, Posts, Posts> replyKafkaTemplate() {
        CompletableFutureReplyingKafkaTemplate<String, Posts, Posts> requestReplyKafkaTemplate =
                new CompletableFutureReplyingKafkaTemplate<>(requestProducerFactory(),
                        replyListenerContainer());
        requestReplyKafkaTemplate.setDefaultTopic(requestTopic);
        requestReplyKafkaTemplate.setDefaultReplyTimeout(Duration.of(replyTimeout, ChronoUnit.MILLIS));
        return requestReplyKafkaTemplate;
    }
    @Bean
    public ProducerFactory<String, Posts> requestProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs2());//producerConfigs()
    }
    @Bean
    public KafkaMessageListenerContainer<String, Posts> replyListenerContainer() {
        ContainerProperties containerProperties = new ContainerProperties(replyTopic);
        return new KafkaMessageListenerContainer<>(replyConsumerFactory(), containerProperties);
    }
    @Bean
    public ConsumerFactory<String, Posts> replyConsumerFactory() {
        JsonDeserializer<Posts> jsonDeserializer = new JsonDeserializer<>();
        jsonDeserializer.addTrustedPackages(Posts.class.getPackage().getName());
        return new DefaultKafkaConsumerFactory<>(consumerConfigs2(), new StringDeserializer(),
                jsonDeserializer);
    }
}
