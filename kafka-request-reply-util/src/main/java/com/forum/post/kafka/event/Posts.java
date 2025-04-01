
package com.forum.post.kafka.event;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class Posts {

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
