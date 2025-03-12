/*
 * Copyright (c) 2024/2025 Binildas A Christudas & Apress
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forum.topic.kafka.event;

//import io.swagger.annotations.ApiModelProperty;

import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * @author <a href="mailto:biniljava<[@.]>yahoo.co.in">Binildas C. A.</a>
 */
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
