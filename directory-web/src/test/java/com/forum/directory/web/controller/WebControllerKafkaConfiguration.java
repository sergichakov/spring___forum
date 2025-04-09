package com.forum.directory.web.controller;

import com.forum.directory.kafka.event.Directories;
import com.forum.directory.web.service.DirectoryWebService;
import com.forum.directory.web.service.DirectoryWebServiceImpl;
import com.forum.kafka.request_reply_util.CompletableFutureReplyingKafkaOperations;
import com.forum.kafka.request_reply_util.CompletableFutureReplyingKafkaTemplate;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebControllerKafkaConfiguration {
    @Value("${spring.embedded.kafka.brokers}")
    private String bootstrapServers;
    @Value("${test.kafka.web.group-id}")
    private String webGroupId;

    @Value("${test.kafka.server.group-id}")
    private String serverGroupID;

    @Value("${kafka.topic.product.request}")
    private String requestTopic;

    @Value("${product.topic.request.numPartitions}")
    private int numPartitions;

    @Autowired
    private Environment env;

    private String replyTopic = "directory-req-reply-topic";

    @Value("${kafka.request-reply.timeout-ms}")
    private Long replyTimeout;

    @Bean
    DirectoryWebService webService3() {
        return new DirectoryWebServiceImpl();
    }

    @Bean
    public CompletableFutureReplyingKafkaOperations<String, Directories, Directories> replyKafkaTemplate() {
        CompletableFutureReplyingKafkaTemplate<String, Directories, Directories> requestReplyKafkaTemplate =
                new CompletableFutureReplyingKafkaTemplate<>(requestProducerFactory(),
                        replyListenerContainer());
        requestReplyKafkaTemplate.setDefaultTopic(requestTopic);
        requestReplyKafkaTemplate.setDefaultReplyTimeout(Duration.of(replyTimeout, ChronoUnit.MILLIS));
        return requestReplyKafkaTemplate;
    }

    @Bean
    public ProducerFactory<String, Directories> requestProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, webGroupId);

        return props;
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return props;
    }

    @Bean
    public KafkaMessageListenerContainer<String, Directories> replyListenerContainer() {
        ContainerProperties containerProperties = new ContainerProperties(replyTopic);
        return new KafkaMessageListenerContainer<>(replyConsumerFactory(), containerProperties);
    }

    @Bean
    public ConsumerFactory<String, Directories> replyConsumerFactory() {
        JsonDeserializer<Directories> jsonDeserializer = new JsonDeserializer<>();
        jsonDeserializer.addTrustedPackages(Directories.class.getPackage().getName());
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(),
                jsonDeserializer);
    }

    @Bean
    public NewTopic replyTopic() {
        Map<String, String> configs = new HashMap<>();
        configs.put("retention.ms", replyTimeout.toString());
        return new NewTopic(replyTopic, numPartitions, (short) 1).configs(configs);
    }

    @Bean
    public Map<String, Object> consumerConfigsForServer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, serverGroupID);
        return props;
    }

    @Bean
    public Map<String, Object> producerConfigsForServer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public ConsumerFactory<String, Directories> requestConsumerFactory() {
        JsonDeserializer<Directories> jsonDeserializer = new JsonDeserializer<>();
        jsonDeserializer.addTrustedPackages(Directories.class.getPackage().getName());
        return new DefaultKafkaConsumerFactory<>(consumerConfigsForServer(), new StringDeserializer(),
                jsonDeserializer);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Directories>> requestReplyListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Directories> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(requestConsumerFactory());
        factory.setReplyTemplate(replyTemplate());
        return factory;
    }

    @Bean
    public ProducerFactory<String, Directories> replyProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigsForServer());
    }

    @Bean
    public KafkaTemplate<String, Directories> replyTemplate() {
        return new KafkaTemplate<>(replyProducerFactory());
    }

    @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic requestTopic() {
        Map<String, String> configs = new HashMap<>();
        configs.put("retention.ms", replyTimeout.toString());
        return new NewTopic(requestTopic, numPartitions, (short) 1).configs(configs);
    }
}
