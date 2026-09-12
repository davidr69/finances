package com.lavacro.finances.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EntityAnnualDTO(
		BigDecimal amount,
		LocalDate mydate,
		String description,
		String reference
) { }
