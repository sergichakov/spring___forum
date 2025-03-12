package com.forum.post.web.model;

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
public class PostWebDto {

    // without "directoryId" don't know can I otherwise
    private UUID postId;
    private Long topicId;
    private String postContent;
    private Timestamp creationDate;
    private Timestamp changeDate;
    private Long userCreatorId;
    private Long userOwnerId;
    private Long numberOfLikes;
}
