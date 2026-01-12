package com.lavacro.finances.model.reports;

import com.lavacro.finances.model.GenericResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BalanceSheet extends GenericResponse {
	private Map<Integer, List<Balance>> balanceList;
}
