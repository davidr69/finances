package com.lavacro.finances.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

import java.math.BigDecimal;

@ToString
@Getter
@Setter
@Entity
@Table(name = "accounts")
public class AccountEntity {
	@Id
	private Integer id;

	private String number;		// account number

	private String description;

	@JoinColumn(name = "type")
	@ManyToOne(targetEntity = AccountTypeEntity.class)
	private AccountTypeEntity type;

	@Column
	private BigDecimal limit;	// unused

	@Column
	private Boolean active;
}
