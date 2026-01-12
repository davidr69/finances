package com.lavacro.finances.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class EntitySummary {
	@Id
	private Integer id;
	private String entity;
	private BigDecimal amount;
	private String money;
	private Integer theyear;
}
