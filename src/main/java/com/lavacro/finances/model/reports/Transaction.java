package com.lavacro.finances.model.reports;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transaction(
	LocalDate date,
	BigDecimal amount,
	String description)
{ }
