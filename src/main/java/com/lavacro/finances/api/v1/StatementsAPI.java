package com.lavacro.finances.api.v1;

import com.lavacro.finances.services.StatementsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class StatementsAPI {
	private final StatementsService statementsService;

	@PostMapping(value = "/api/v1/statement_merge")
	public String statementMerge(@RequestBody Map<Integer, Character> selections, @RequestParam("account") Integer account) {
		log.info("merge: {}", selections);
		statementsService.mergeSelections(selections, account);
		return "{\"status\":\"OK\"}";
	}

	@PutMapping(value = "/api/v1/update_staging_vendor")
	public String updateEntity(@RequestParam("entity") Integer entity, @RequestParam("id") Integer id) {
		statementsService.updateEntity(id, entity);
		return "{\"status\":\"OK\"}";
	}
}
