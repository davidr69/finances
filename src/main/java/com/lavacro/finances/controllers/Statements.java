package com.lavacro.finances.controllers;

import com.lavacro.finances.services.StatementsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
@Slf4j
@RequiredArgsConstructor
public class Statements {
	private final StatementsService statementsService;

	@GetMapping(value = "/merge_statement")
	public String mergeStatement(
			Model model,
			@RequestParam("year") Integer year,
			@RequestParam("account") Integer account
	) {
		log.info("Merge statement: year={}, account={}", year, account);
		model.addAttribute("statementData", statementsService.getStatement(account));
		return "merge_statement";
	}
}
