package com.forum.topic.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopicWebDto {

    private Long postId;
    private String topicLabel;
    private Long directoryId;

    private String postContent;
    private Timestamp creationDate;
    private Timestamp changeDate;

    private Long userOwnerId;

}
