package com.lavacro.finances.api.v1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class StatementsAPI {
	@PostMapping(value = "/api/v1/statement_merge")
	public String statementMerge(@RequestBody Map<Integer, Character> selections) {
		log.info("merge: {}", selections);
		return "{\"status\":\"OK\"}";
	}
}
