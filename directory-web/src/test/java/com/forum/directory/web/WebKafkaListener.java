package com.forum.directory.web;

import com.forum.directory.kafka.event.Directories;
import com.forum.directory.kafka.event.OperationDirectoryKafka;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;

@Configuration
public class WebKafkaListener {
    @KafkaListener(topics = "${kafka.topic.product.request}", containerFactory = "requestReplyListenerContainerFactory")
    @SendTo
    public Directories listenConsumerRecord(ConsumerRecord<String, Directories> record) {
        Directories directoriesResult = new Directories();
        Directories directories = record.value();
        directoriesResult.setOperation(OperationDirectoryKafka.SUCCESS);
        directoriesResult.setMax(0L);
        directoriesResult.setDirectories(directories.getDirectories());
        directoriesResult.setPage(directories.getPage());
        directoriesResult.setNumberPerPage(directories.getNumberPerPage());
        System.out.println("gotten Text from Kafka");
        return directories;
    }
}
