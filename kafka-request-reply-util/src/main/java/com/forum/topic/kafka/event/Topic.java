
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
	//    private Long topicId;
	private String postContent;
	private Timestamp creationDate;
	private Timestamp changeDate;
	//    private Long userCreatorId;
	private Long userOwnerId;
	//@ApiModelProperty(position = 1)
/*	private Long directoryId; // String directoryId


	private Long order;

	private Long subDirId;

	private Timestamp creationDate;

    private	String topicId;

	private String name;

 */
}
