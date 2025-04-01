
package com.forum.directory.repo.model;

//import org.springframework.data.annotation.Id;
//import javax.persistence.SequenceGenerator;

import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.protocol.types.Field;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name ="directories")
public class DirectoryThemeEntity {

	//@SequenceGenerator(name="seq",sequenceName="or_db_seq")
	//@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "directory_id")
	private Long directoryId;

	@Column(name = "order_num")
	private Long order;

	@Column(name = "sub_dir_id")
	private Long subDirId;

	@Column(name = "creation_date")
	private Timestamp creationDate;

	@Column(name = "topic_id")
	private Long topicId;

	@Column(name = "dir_name")
	private String name;
}
