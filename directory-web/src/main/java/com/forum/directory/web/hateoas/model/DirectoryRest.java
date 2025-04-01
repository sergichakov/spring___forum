
package com.forum.directory.web.hateoas.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.hateoas.RepresentationModel;

import java.sql.Timestamp;
import java.util.UUID;


@Data
//@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DirectoryRest extends RepresentationModel<DirectoryRest> {
    @Id
    private Long directoryId; //String directoryId
    private Long order;

    private Long subDirId;

    private Timestamp creationDate;

    private Long topicId;

    private String name;
}
/*
public class DirectoryRest extends RepresentationModel<DirectoryRest> {

	@Id
	private String directoryId;
	private String name;
	private String code;;
	private String title;
	private Double price;

	public DirectoryRest() {}
	
	public String getDirectoryId() {
		return directoryId;
	}

	public void setDirectoryId(String directoryId) {
		this.directoryId = directoryId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	
	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Product [directoryId=").append(directoryId).append(", name=").append(name).append(", code=").append(code)
				.append(", title=").append(title).append(", price=").append(price)
				.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DirectoryRest other = (DirectoryRest) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
*/