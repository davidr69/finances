package com.lavacro.finances.api.v1;

import com.lavacro.finances.model.reports.BalanceSheet;

import com.lavacro.finances.services.ReportsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/reports")
public class ReportsAPI {
	private final ReportsService reportsService;

	public ReportsAPI(ReportsService reportsService) {
		this.reportsService = reportsService;
	}

	@GetMapping(value = "/balanceSheet")
	public ResponseEntity<BalanceSheet> balanceSheet(
			@RequestParam("account") final Integer account
	) {
		return new ResponseEntity<>(reportsService.balanceSheet(account), HttpStatus.OK);
	}
}
