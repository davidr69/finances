package com.lavacro.finances.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Getter
@Setter
@Entity
public class YearEntity {
	@Id
	private Integer year;
}
