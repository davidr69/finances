package com.lavacro.finances.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "users", schema = "rbac")
public class RbacUsersEntity {
	@Id
	private Integer id;

	private String name;

	private String password;

	@Column(name = "login_attempts")
	private Integer loginAttempts;

	@Column(name = "last_login")
	private LocalDateTime lastLogin;

	private Boolean locked;

	@Column(name = "locked_ip")
	private String lockedIp;
}
