package com.lavacro.finances.api.v1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
public class UploadAPI {
	@PostMapping("/api/v1/upload_statement")
	public ResponseEntity<?> uploadStatement(@RequestParam("file") MultipartFile file) {
		log.info("Uploading statement ...");
		log.info("File name: {}", file.getOriginalFilename());
		log.info("File size: {}", file.getSize());
		log.info("File content: {}", file.getContentType());
		return null;
	}
}
