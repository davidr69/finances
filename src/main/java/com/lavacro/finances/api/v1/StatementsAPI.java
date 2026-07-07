package com.lavacro.finances.api.v1;

import com.lavacro.finances.kafka.service.DecisionService;
import com.lavacro.finances.services.StatementsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1")
@Slf4j
@RequiredArgsConstructor
public class StatementsAPI {
	private final StatementsService statementsService;
	private final DecisionService decisionService;

	private static final String OK_STATUS = """
{"status":0,"message":"OK"}
""";
	private static final String ERROR_STATUS = """
{"status":1,"message":"ERROR"}
""";

	@PostMapping(value = "/statement_merge")
	public String statementMerge(@RequestBody Map<Integer, String> selections, @RequestParam("account") Integer account) {
		log.info("merge: {}", selections);
		try {
			statementsService.mergeSelections(selections, account);
			return OK_STATUS;
		} catch(Exception e) {
			log.error("Error during 'statementMerge': {}", e.getMessage());
			return ERROR_STATUS;
		}
	}

	@PutMapping(value = "/update_staging_vendor")
	public String updateEntity(@RequestParam("entity") Integer entity, @RequestParam("id") Integer id) {
		log.info("updateEntity: set vendor for id {} to {}", id, entity);
		try {
			statementsService.updateEntity(id, entity);
			return OK_STATUS;
		} catch(Exception e) {
			log.error("Error during 'updateEntity': {}", e.getMessage());
			return ERROR_STATUS;
		}
	}

	@PutMapping(value = "/refresh_vectors")
	public String refreshVectors() {
		log.info("Refresh vectors ...");
		try {
			decisionService.refreshVectors();
			return OK_STATUS;
		} catch(Exception e) {
			log.error("Error during 'refreshVectors': {}", e.getMessage());
			return ERROR_STATUS;
		}
	}
}
