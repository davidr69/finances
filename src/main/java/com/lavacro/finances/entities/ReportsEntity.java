package com.lavacro.finances.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
public class ReportsEntity {
	@Id
	@Column(name = "entity_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer entityId;

	@Column(name = "entity_name", nullable = false)
	private String entityName;

}

/*
 entity_id |  amount  |    date    |   entity_name    |              reference              |  total   | action_id
-----------+----------+------------+------------------+-------------------------------------+----------+-----------
       500 |   -35.00 | 2021-09-02 | Citgo            |                                     |   -35.00 |     27187
    999900 |     5.50 | 2021-01-31 | Credit           |                                     |     5.50 |     26419
 */
