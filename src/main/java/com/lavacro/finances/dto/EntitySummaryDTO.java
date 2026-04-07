package com.lavacro.finances.dto;

import java.math.BigDecimal;

public record EntitySummaryDTO(
	String entity,
	BigDecimal amount,
	String money,
	Integer theyear
) {
}
