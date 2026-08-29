package com.lavacro.finances.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "entities")
@Getter
@Setter
public class EntityEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "acct")
	private String account;

	@Column(nullable = false)
	private String description;

	private String address;

	@Column(name = "bank_alias")
	private String aliases;

	private String embedding;

	@Column(name = "rag_updated", nullable = true)
	private LocalDateTime ragUpdated;

	@Column(name = "vector_sync", nullable = true)
	private LocalDateTime vectorSynced;

	@Transient
	private Boolean validated;
}
