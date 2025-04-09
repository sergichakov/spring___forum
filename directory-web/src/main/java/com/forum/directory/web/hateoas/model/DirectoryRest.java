
package com.forum.directory.web.hateoas.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.hateoas.RepresentationModel;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Getter
@Setter
public class DirectoryRest extends RepresentationModel<DirectoryRest> {
    @Id
    private Long directoryId;
    private Long order;

    private Long subDirId;

    private Timestamp creationDate;

    private Long topicId;

    private String name;
}