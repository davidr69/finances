package com.lavacro.finances.dto;

import org.springframework.data.annotation.Id;

public record AccountDTO(
	@Id Integer id,
	String account,
	String description,
	String action
) {
}
