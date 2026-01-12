package com.lavacro.finances.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;

@Entity
@Getter
@Setter
@Table(name = "users", schema = "rbac")
public class RbacUsersEntity {
	@Id
	private Integer id;

	private boolean authenticated;
}
