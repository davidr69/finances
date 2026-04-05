package com.lavacro.finances.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EntityDTO(
		BigDecimal amount,
		LocalDate mydate,
		String description,
		String reference
) { }
