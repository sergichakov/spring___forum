
package com.forum.post.kafka.event;


import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;


@Getter
@Setter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
	private UUID postId;
	private Long topicId;
	private String postContent;
	private Timestamp creationDate;
	private Timestamp changeDate;
	private Long userCreatorId;
	private Long userOwnerId;
	private Long numberOfLikes;
}
