package com.lavacro.finances.model.reports;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class SummaryRow implements Comparable<SummaryRow> {
	private Integer rank;
	private String entity;
	private Map<Integer, String> columns;
	private BigDecimal total;
	private String formattedTotal;

	public int compareTo(SummaryRow row) {
		return total.compareTo(row.total);
	}
}
