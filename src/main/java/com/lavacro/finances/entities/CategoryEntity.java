package com.lavacro.finances.entities;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;



@Table(name = "categories")
public record CategoryEntity(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer id,

	String description
) {}
