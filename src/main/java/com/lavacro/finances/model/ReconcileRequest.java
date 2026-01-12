package com.lavacro.finances.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReconcileRequest {
	private List<Integer> entries;
}
