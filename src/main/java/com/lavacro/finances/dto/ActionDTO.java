package com.lavacro.finances.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Sequence;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;

@Data
@Table(name = "action")
public class ActionDTO {
	private static NumberFormat nf;

	static {
		nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
	}

	@Id
	@Sequence(value = "action_seq")
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
