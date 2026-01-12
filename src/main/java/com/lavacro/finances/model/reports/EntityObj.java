package com.lavacro.finances.model.reports;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class EntityObj {
	private String name;
	private BigDecimal total;
	private List<Transaction> transactions;
}
