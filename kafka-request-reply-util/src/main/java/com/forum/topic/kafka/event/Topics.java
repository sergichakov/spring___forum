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

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author <a href="mailto:biniljava<[@.]>yahoo.co.in">Binildas C. A.</a>
 */
@Getter
@Setter
public class Topics {

	private OperationKafka operation;

//	public static final String CREATE = "Create";
//	public static final String DELETE = "Delete";
//	public static final String DELETE_ALL = "Delete_All";
//	public static final String UPDATE = "Update";
//	public static final String RETREIVE_ALL = "Retreive_All";
//	public static final String RETREIVE_DETAILS = "Retreive_Details";
//
//	public static final String SUCCESS = "Success";
//	public static final String FAILURE = "Failure";
//
//	private String operation;
	private List<Topic> topics;
	private Integer page;
	private Integer numberPerPage;
	private Long headerUserId;
	private UserDetailsRole role;
	public OperationKafka getOperation() {
		return operation;
	}
	public void setOperation(OperationKafka operation) {
		this.operation = operation;
	}

	public List<Topic> getTopics() {
		return topics;
	}
	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}
}
