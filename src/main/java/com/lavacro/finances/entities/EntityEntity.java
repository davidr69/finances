package com.lavacro.finances.entities;

import lombok.Getter;
import lombok.Setter;

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
}
