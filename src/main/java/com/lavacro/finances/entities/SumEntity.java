package com.lavacro.finances.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class SumEntity {
	@Id
	private Integer rownum;
	private BigDecimal balance;
}
