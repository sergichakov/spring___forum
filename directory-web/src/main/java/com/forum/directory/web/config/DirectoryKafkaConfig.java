
package com.forum.directory.web.config; //// i don't know Why changed from "com.forum.directory"  -- without web

import java.util.HashMap;
import java.util.Map;

import com.forum.directory.kafka.event.Directories;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import com.forum.kafka.request_reply_util.CompletableFutureReplyingKafkaOperations;
import com.forum.kafka.request_reply_util.CompletableFutureReplyingKafkaTemplate;


@Configuration
public class DirectoryKafkaConfig {
//	@Autowired
//	private KafkaMessageListenerContainer<String, Directories> kafkaMessageListenerContainer;
	 @Value("${spring.kafka.bootstrap-servers}")
	  private String bootstrapServers;

	  @Value("${spring.kafka.consumer.group-id}")
	  private String groupId;

	  @Value("${kafka.topic.product.request}")
	  private String requestTopic;

	  @Value("${product.topic.request.numPartitions}")
	  private int numPartitions;

	  @Value("${kafka.topic.product.reply}")
	  private String replyTopic;

	  @Value("${kafka.request-reply.timeout-ms}")
	  private Long replyTimeout;

	  @Bean
	  public Map<String, Object> consumerConfigs() {
	    Map<String, Object> props = new HashMap<>();
	    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
	    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

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
	  public CompletableFutureReplyingKafkaOperations<String, Directories, Directories> replyKafkaTemplate() {
	    CompletableFutureReplyingKafkaTemplate<String, Directories, Directories> requestReplyKafkaTemplate =
	        new CompletableFutureReplyingKafkaTemplate<>(requestProducerFactory(),
//					kafkaMessageListenerContainer);
					////may be it is wrong but i TRYING
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
	  public ConsumerFactory<String, Directories> replyConsumerFactory() {
	    JsonDeserializer<Directories> jsonDeserializer = new JsonDeserializer<>();
	    jsonDeserializer.addTrustedPackages(Directories.class.getPackage().getName());
	    return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(),
	        jsonDeserializer);
	  }

	  @Bean
	  public KafkaMessageListenerContainer<String, Directories> replyListenerContainer() {
	    ContainerProperties containerProperties = new ContainerProperties(
		//		new TopicPartitionOffset(replyTopic, 0));      //
		   replyTopic);
		////may be it is wrong but i Add new Line new TopicPartitionOffset("reply", 0));
	    return new KafkaMessageListenerContainer<>(replyConsumerFactory(), containerProperties);
	  }

	  @Bean
	  public KafkaAdmin admin() {
	    Map<String, Object> configs = new HashMap<>();
	    configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
	    return new KafkaAdmin(configs);
	  }

	  @Bean
	  public NewTopic replyTopic() {
	    Map<String, String> configs = new HashMap<>();
	    configs.put("retention.ms", replyTimeout.toString());
	    return new NewTopic(replyTopic, numPartitions, (short) 1).configs(configs);
	  }
}
