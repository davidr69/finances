package com.lavacro.finances.api.v1;

import com.lavacro.finances.kafka.service.NotifyAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@RestController
@Slf4j
public class UploadAPI {
	private final NotifyAgent notifyAgent;

	public UploadAPI(NotifyAgent notifyAgent) {
		this.notifyAgent = notifyAgent;
	}

	@PostMapping("/api/v1/upload_statement")
	public ResponseEntity<Map<String,Object>> uploadStatement(
		HttpServletRequest request,
		@RequestParam("file") MultipartFile file
	) {
		log.info("Uploading statement ...");
		log.info("File name: {}", file.getOriginalFilename());
		log.info("File size: {}", file.getSize());
		log.info("File content: {}", file.getContentType());

		int accountId;

		try {
			accountId = Integer.parseInt(request.getHeader("accountId"));
		} catch (NumberFormatException e) {
			log.error("Invalid accountId format", e);
			return new ResponseEntity<>(Map.of("message", "Invalid accountId format", "code", "2"), HttpStatus.BAD_REQUEST);
		}

		byte[] content;

		try {
			content = file.getBytes();
		} catch (IOException e) {
			log.error("Error occurred while reading file bytes", e);
			return new ResponseEntity<>(Map.of("message", "Error occurred while reading file bytes", "code", "1"), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		notifyAgent.send(Objects.requireNonNull(file.getOriginalFilename()), accountId, content);
		return new ResponseEntity<>(Map.of("message", "File uploaded successfully", "code", "0"), HttpStatus.OK);
	}
}
