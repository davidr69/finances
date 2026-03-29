package com.lavacro.finances.entities;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticatedDTO {
	@Id
	private Integer id;
	private Boolean authenticated;
}
