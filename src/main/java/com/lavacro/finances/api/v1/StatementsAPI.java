package com.lavacro.finances.api.v1;

import com.lavacro.finances.kafka.service.DecisionService;
import com.lavacro.finances.kafka.service.NotifyAgent;
import com.lavacro.finances.model.GenericResponse;
import com.lavacro.finances.services.StatementsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/v1")
@Slf4j
@RequiredArgsConstructor
public class StatementsAPI {
	private final StatementsService statementsService;
	private final DecisionService decisionService;
	private final NotifyAgent notifyAgent;

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

	@PostMapping("/upload_statement")
	public ResponseEntity<GenericResponse> uploadStatement(
		HttpServletRequest request,
		@RequestParam("file") MultipartFile file
	) {
		log.info("Uploading statement ...");
		log.info("File name: {}", file.getOriginalFilename());
		log.info("File size: {}", file.getSize());
		log.info("File content: {}", file.getContentType());

		int accountId;
		int year;

		try {
			accountId = Integer.parseInt(request.getHeader("accountId"));
			year = Integer.parseInt(request.getHeader("year"));
		} catch (NumberFormatException e) {
			log.error("Invalid accountId or year format", e);
			GenericResponse resp = new GenericResponse();
			resp.setMessage("Invalid accountId or year format");
			resp.setCode(1);
			return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
		}

		byte[] content;

		try {
			content = file.getBytes();
		} catch (IOException e) {
			log.error("Error occurred while reading file bytes", e);
			GenericResponse resp = new GenericResponse();
			resp.setMessage("Error occurred while reading file bytes");
			resp.setCode(1);
			return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		try {
		GenericResponse resp = notifyAgent.send(Objects.requireNonNull(file.getOriginalFilename()), accountId, year, content).get();
			return new ResponseEntity<>(resp, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while sending message", e);
			GenericResponse resp = new GenericResponse();
			resp.setMessage("Error occurred while sending message");
			resp.setCode(1);
			return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
