package com.lavacro.finances.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionDTO(
	Integer sequence,
	BigDecimal amount,
	LocalDate mydate,
	String reference,
	Boolean isReconciled,
	Boolean isVisible,
	String entityName,
	String method,
	String runningTotal
) {
}
