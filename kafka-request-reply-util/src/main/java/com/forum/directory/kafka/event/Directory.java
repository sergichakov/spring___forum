
package com.forum.directory.kafka.event;


import lombok.*;

import java.sql.Timestamp;


@Getter
@Setter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Directory {

    private Long directoryId;

    private Long order;

    private Long subDirId;

    private Timestamp creationDate;

    private Long topicId;

    private String name;
}
