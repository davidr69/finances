package com.lavacro.finances.kafka.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class DecisionModel implements Serializable {
	String decision;	// accept, change
	Integer transactionId;
	Integer originalVendorId;
	String originalVendorName;
	Integer newVendorId;
	String newVendorName;
}
