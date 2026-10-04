package com.lavacro.finances.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

@Getter
@Setter
public class ReportsDTO {
	@Id
	@Column(value = "entity_id")
	private Integer entityId;

	@Column(value = "entity_name")
	private String entityName;

}

/*
 entity_id |  amount  |    date    |   entity_name    |              reference              |  total   | action_id
-----------+----------+------------+------------------+-------------------------------------+----------+-----------
       500 |   -35.00 | 2021-09-02 | Citgo            |                                     |   -35.00 |     27187
    999900 |     5.50 | 2021-01-31 | Credit           |                                     |     5.50 |     26419
 */
