
package com.forum.topic.repo.model;

//import org.springframework.data.annotation.Id;
//import javax.persistence.SequenceGenerator;

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

	//@SequenceGenerator(name="seq",sequenceName="or_db_seq")
	//@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
	//@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@Id
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
	@UpdateTimestamp		//// надо это поменять и в DirectoryThemeEntity
	@Temporal(TemporalType.TIMESTAMP)
	@Column (name="change_date")
	private Timestamp changeDate;

	@Column (name="user_Owner_id")
	private Long userOwnerId;


}
//	private Long postId;
//	private String topicLabel;
//	private Long directoryId;
//	//    private Long topicId;
//	private String postContent;
//	private Timestamp creationDate;
//	private Timestamp changeDate;
//	//    private Long userCreatorId;
//	private Long userOwnerId;