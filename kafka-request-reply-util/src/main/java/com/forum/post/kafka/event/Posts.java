
package com.forum.post.kafka.event;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Posts {
	private OperationKafka operation;
	private List<Post> posts;
	private Integer page;
	private Integer numberPerPage;
	private Long headerUserId;
	public OperationKafka getOperation() {
		return operation;
	}
	public void setOperation(OperationKafka operation) {
		this.operation = operation;
	}

	public List<Post> getPosts() {
		return posts;
	}
	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}
}
