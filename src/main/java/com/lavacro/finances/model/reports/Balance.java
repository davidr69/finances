package com.lavacro.finances.model.reports;

import java.math.BigDecimal;

public record Balance(
	Integer month,
	BigDecimal credits,
	BigDecimal debits,
	BigDecimal diff,
	BigDecimal balance) { }
