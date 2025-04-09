package com.forum.directory.kafka.event;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class Directories {

    public OperationDirectoryKafka operation;
    private List<Directory> directories;
    private Integer page;
    private Integer numberPerPage;
    private Long max;

    public OperationDirectoryKafka getOperation() {
        return operation;
    }

    public void setOperation(OperationDirectoryKafka operation) {
        this.operation = operation;
    }

    public List<Directory> getDirectories() {
        return directories;
    }

    public void setDirectories(List<Directory> directories) {
        this.directories = directories;
    }
}
