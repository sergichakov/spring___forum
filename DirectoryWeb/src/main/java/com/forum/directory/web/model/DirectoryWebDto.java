package com.forum.directory.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DirectoryWebDto {

    // without "directoryId" don't know can I otherwise
    private Long directoryId;

    private Long order;

    private Long subDirId;

    private Timestamp creationDate;

    private Long topicId;

    private String name;
}
