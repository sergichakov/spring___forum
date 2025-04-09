
package com.forum.topic.repo.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Timestamp;
import java.util.UUID;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name ="topics")
public class TopicEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)

	@Column(name="post_id")
	private Long postId;
	@Column(name="topic_id")
	private String topicLabel;
	@Column(name="directory_id")
	private Long directoryId;
	@Column(name="post_content")
	private String postContent;
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column (name="creation_date")
	private Timestamp creationDate;
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column (name="change_date")
	private Timestamp changeDate;

	@Column (name="user_Owner_id")
	private Long userOwnerId;
}