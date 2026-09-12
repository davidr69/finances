package com.lavacro.finances.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "account_type")
public record AccountTypeDTO(
	@Id Long id,
	String name,
	String description
) {
}
