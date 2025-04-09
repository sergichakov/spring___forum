
package com.forum.topic.kafka.event;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class Topics {

	private OperationKafka operation;
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
