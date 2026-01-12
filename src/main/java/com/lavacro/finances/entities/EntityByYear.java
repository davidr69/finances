package com.lavacro.finances.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
public class EntityByYear {
	@Id
	private Integer sequence;

	@Column(nullable = false)
	private BigDecimal amount;

	@Column(nullable = false)
	private LocalDate mydate;

	@Column(nullable = false)
	private String description;

	@Column
	private String reference;

	@Column
	private BigDecimal total;
}
