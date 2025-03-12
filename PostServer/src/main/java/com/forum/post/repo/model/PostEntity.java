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

/**
 * @author <a href="mailto:biniljava<[@.]>yahoo.co.in">Binildas C. A.</a>
 */
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
