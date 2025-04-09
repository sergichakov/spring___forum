
package com.forum.topic.kafka.event;

//import io.swagger.annotations.ApiModelProperty;

import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;


@Getter
@Setter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Topic {
	private Long postId;
	private String topicLabel;
	private Long directoryId;
	private String postContent;
	private Timestamp creationDate;
	private Timestamp changeDate;
	private Long userOwnerId;

}
