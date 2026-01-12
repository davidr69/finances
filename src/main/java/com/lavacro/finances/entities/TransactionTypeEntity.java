package com.lavacro.finances.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

@ToString
@Entity
@Getter
@Setter
@Table(name = "trans_type")
public class TransactionTypeEntity {
	@Id
	private Integer id;

	@Column(nullable = false)
	private String description;

	@Column(name = "credit_debit")
	private String creditDebit;
}
