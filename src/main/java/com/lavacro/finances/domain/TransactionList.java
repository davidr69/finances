package com.lavacro.finances.domain;

import com.lavacro.finances.entities.TransactionEntity;
import com.lavacro.finances.model.GenericResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TransactionList extends GenericResponse {
	private List<TransactionEntity> transactions;
}
