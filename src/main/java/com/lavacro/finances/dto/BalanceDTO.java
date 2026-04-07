package com.lavacro.finances.dto;

import java.math.BigDecimal;

public record BalanceDTO(
	Integer month,
	BigDecimal credits,
	BigDecimal debits,
	BigDecimal diff,
	BigDecimal balance) { }
