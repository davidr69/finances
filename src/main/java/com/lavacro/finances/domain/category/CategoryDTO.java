package com.lavacro.finances.domain.category;

import org.springframework.data.annotation.Id;

public record CategoryDTO(
	@Id Integer id,
	String description
) {}
