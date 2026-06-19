package com.lavacro.finances.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StatementDTO(
	Integer actionId,
	LocalDate date,
	boolean isNewEntity,
	String entity,
	BigDecimal amount
) { }
