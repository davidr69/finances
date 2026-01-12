package com.lavacro.finances.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "action")
public class ActionEntity {
	private static NumberFormat nf;

	static {
		nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
	@SequenceGenerator(name = "sequence_generator", sequenceName = "action_seq", allocationSize = 1)
	private Integer sequence;

	private Integer entity;

	private Integer account;

	private BigDecimal amount;

	private LocalDate mydate;

	private Integer method;

	private String reference;

	private Boolean visible;

	private Boolean reconciled;

	private Integer category;

	@Transient
	public String getFormattedDate() {
		return nf.format(this.amount);
	}
}
