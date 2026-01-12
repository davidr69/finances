package com.lavacro.finances.controllers;

import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Transaction {

	private static final Logger log = org.slf4j.LoggerFactory.getLogger(Transaction.class);

	@GetMapping("/transaction")
	public String makePage() {
		log.info("New transaction ...");
		return "new_transaction";
	}
}
