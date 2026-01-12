package com.lavacro.finances.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IncludesModifyRequest {
	private List<Integer> add;
	private List<Integer> remove;
}
