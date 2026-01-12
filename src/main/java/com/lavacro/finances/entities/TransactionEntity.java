package com.lavacro.finances.entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class TransactionEntity {
	@Id
	private Integer sequence;

	private BigDecimal amount;

	private LocalDate mydate;

	private String reference;

	private Boolean reconciled;

	@Getter(AccessLevel.NONE)
	private Boolean visible;

	private String entity;

	private String method;

	// cumulative
	@Transient
	private String runningTotal;

	public boolean isVisible() {
		return Boolean.TRUE.equals(visible);
	}
}
