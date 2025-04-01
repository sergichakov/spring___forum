package com.forum.post.repo.model;

//import org.springframework.data.annotation.Id;
//import javax.persistence.SequenceGenerator;

import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Timestamp;
import java.util.UUID;


@Data
@NoArgsConstructor
@Entity
@Table(name ="posts")
public class PostEntity {

	//@SequenceGenerator(name="seq",sequenceName="or_db_seq")
	//@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
	//@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@Id
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	@GenericGenerator(
			name = "UUID",
			strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Column(name="post_id")
	private UUID postId;
	@Column(name="topic_id")
	private Long topicId;

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
	@Column (name="user_creator_id")
	private Long userCreatorId;
	@Column (name="user_Owner_id")
	private Long userOwnerId;
	@Column (name="number_of_likes")
	private Long numberOfLikes;

}
