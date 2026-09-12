package com.lavacro.finances.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "entities")
@Data
public class EntityDTO {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer id;

	@Column(value = "acct")
	String account;

	String description;

	String address;

	@Column(value = "bank_alias")
	String aliases;

	String embedding;

	@Column(value = "rag_updated")
	LocalDateTime ragUpdated;

	@Column(value = "vector_sync")
	LocalDateTime vectorSynced;

	@Transient
	private boolean validated;
}
