package com.lavacro.finances.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class AuthenticatedEntity {
	@Id
	private Integer id;
	private Boolean authenticated;
}
