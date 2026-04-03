package com.lavacro.finances.dto;

import java.math.BigDecimal;
import java.util.List;

public record EntityTotalsDTO(
	String entityName,
	BigDecimal total,
	List<EntityDTO> entities
) {
}
