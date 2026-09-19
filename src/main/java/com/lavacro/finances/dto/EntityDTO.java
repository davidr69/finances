package com.lavacro.finances.dto;

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
	private Integer id;

	@Column(value = "acct")
	private String account;

	private String description;

	private String address;

	@Column(value = "bank_alias")
	private String aliases;

	private String embedding;

	@Column(value = "rag_updated")
	private LocalDateTime ragUpdated;

	@Column(value = "vector_sync")
	private LocalDateTime vectorSynced;

	@Transient
	public boolean isValidated() {
		return embedding != null;
	}
}
