package com.lavacro.finances.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class BalanceSheetEntity {
	@Id
	@Column
	private Long rownum;

	@Column
	private BigDecimal withdrawals;

	@Column
	private BigDecimal deposits;

	@Column(name = "surp_def")
	private BigDecimal surplusOrDeficit;

	@Column(name = "yr")
	private Integer year;

	@Column(name = "mo")
	private Integer month;
}
