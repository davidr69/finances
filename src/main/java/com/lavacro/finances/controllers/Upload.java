package com.lavacro.finances.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class Upload {
	@GetMapping(value = "/upload")
	public String makePage() {
		log.info("Upload statement");
		return "upload_statement";
	}
}
