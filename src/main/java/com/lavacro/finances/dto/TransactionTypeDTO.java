package com.lavacro.finances.dto;

import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

@Table(name = "trans_type")
public record TransactionTypeDTO (
	@Id
	Integer id,

	String description,

	@Column(value = "credit_debit")
	String creditDebit
) {}
