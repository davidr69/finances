package com.lavacro.finances.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class NewTransaction {
	@NotNull(message = "month is required")
	private Integer month;

	@NotNull(message = "day is required")
	private Integer day;

	@NotNull(message = "year is required")
	private Integer year;

	@NotNull(message = "account is required")
	private Integer account;

	@NotNull(message = "entity is required")
	private Integer entity;

	@NotEmpty(message = "amount is required")
	private String amount;

	@NotNull(message = "method is required")
	private Integer method;

	private String reference;
	private Integer category;
}
