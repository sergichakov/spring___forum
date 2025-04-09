
package com.forum.directory.repo.model;

//import org.springframework.data.annotation.Id;
//import javax.persistence.SequenceGenerator;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Entity
@Table(name = "directories")
public class DirectoryThemeEntity {

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
