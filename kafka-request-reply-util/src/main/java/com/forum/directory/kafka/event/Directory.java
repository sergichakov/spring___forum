
package com.forum.directory.kafka.event;

//import io.swagger.annotations.ApiModelProperty;

import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;


@Getter
@Setter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Directory {

	//@ApiModelProperty(position = 1)
	private Long directoryId; // String directoryId
/*
	//@ApiModelProperty(position = 2)
	private String name;
	
	//@ApiModelProperty(position = 3)
	private String code;;
	
	//@ApiModelProperty(position = 4)
	private String title;
	
	//@ApiModelProperty(position = 5)
	private Double price;
	*/
	private Long order;

	private Long subDirId;

	private Timestamp creationDate;

    private	Long topicId;

	private String name;
}
