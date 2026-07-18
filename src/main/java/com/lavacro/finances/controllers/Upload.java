package com.lavacro.finances.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class Upload {
	@GetMapping(value = "/upload")
	@PreAuthorize("hasAuthority('PERMISSION_UPLOAD_STATEMENT')")
	public String makePage() {
		log.info("Upload statement");
		return "upload_statement";
	}
}
