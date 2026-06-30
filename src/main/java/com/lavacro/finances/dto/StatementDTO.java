package com.lavacro.finances.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StatementDTO(
	Integer actionId,
	LocalDate date,
	BigDecimal amount,
	String statementVendor,
	Integer vectorId,
	Integer llmId,
	String vectorVendor,
	String llmVendor,
	boolean newVendor
) { }
