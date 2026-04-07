package com.lavacro.finances.dto;

import java.math.BigDecimal;

public record BalanceSheetDTO(
	BigDecimal withdrawals,
	BigDecimal deposits,
	BigDecimal surplusOrDeficit,
	Integer year,
	Integer month
) {
}
