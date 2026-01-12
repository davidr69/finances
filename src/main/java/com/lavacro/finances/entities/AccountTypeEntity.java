package com.lavacro.finances.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@ToString
@Getter
@Setter
@Entity
@Table(name = "account_type")
public class AccountTypeEntity {
	@Id
	private Integer id;

	@Column
	private String description;

	@Column
	private String action;		// + or -
}
