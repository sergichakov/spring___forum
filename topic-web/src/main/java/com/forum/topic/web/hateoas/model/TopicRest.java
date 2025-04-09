
package com.forum.topic.web.hateoas.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.hateoas.RepresentationModel;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TopicRest extends RepresentationModel<TopicRest> {
    @Id
    private Long postId;
    private String topicLabel;
    private Long directoryId;
    private String postContent;
    private Timestamp creationDate;
    private Timestamp changeDate;
    private Long userOwnerId;
}
