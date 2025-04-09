
package com.forum.post.web.hateoas.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.hateoas.RepresentationModel;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@Getter
@Setter
public class PostRest extends RepresentationModel<PostRest> {
    @Id
    private UUID postId;
    private Long topicId;
    private String postContent;
    private Timestamp creationDate;
    private Timestamp changeDate;
    private Long userCreatorId;
    private Long userOwnerId;
    private Long numberOfLikes;
}