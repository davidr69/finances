package com.lavacro.finances.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@ToString
@Entity
@Getter
@Setter
@Table(name = "action")
public class ActionJoinEntity {
	@Id
	private Integer sequence;

	@JoinColumn(name = "entity", insertable = false, updatable = false)
	@ManyToOne(targetEntity = EntityEntity.class, fetch = FetchType.EAGER)
	private EntityEntity entityEntity;

	@JoinColumn(name = "account")
	@ManyToOne(targetEntity = AccountEntity.class)
	private AccountEntity accountEntity;

	@Column(name = "amount", nullable = false)
	private BigDecimal amount;

	@Column(name = "mydate", nullable = false)
	private LocalDate date;

	@JoinColumn(name = "method")
	@ManyToOne(targetEntity = TransactionTypeEntity.class)
	private TransactionTypeEntity method;

	@Column(name = "reference")
	private String reference;

	@Column
	private Boolean visible;

	@Column
	private Boolean reconciled;

	@JoinColumn(name = "category")
	@ManyToOne(targetEntity = CategoryEntity.class)
	private CategoryEntity categoryEntity;

	@Column
	private LocalDateTime inserted;

	@Column
	private LocalDateTime updated;
}
