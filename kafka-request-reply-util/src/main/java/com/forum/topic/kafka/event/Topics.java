
package com.forum.topic.kafka.event;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


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
