package com.lavacro.finances.controllers;

import com.lavacro.finances.dao.ReportsDao;
import com.lavacro.finances.model.reports.SummaryRow;

import com.lavacro.finances.services.ReportsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("reports")
public class Reports {
	private final ReportsDao reportsDao;
	private final ReportsService reportsService;

	public Reports(ReportsDao reportsDao, ReportsService reportsService) {
		this.reportsDao = reportsDao;
		this.reportsService = reportsService;
	}

	@GetMapping(value = "/byEntity")
	public String byEntity(
			Model model,
			@RequestParam("account") final Integer account,
			@RequestParam("year") final Integer year
	) {
		model.addAttribute("data", reportsDao.byEntity(account, year));
		return "report_byEntity";
	}

	@GetMapping(value = "/summaryByYear")
	public String summaryByYear(
			Model model,
			@RequestParam("account") final Integer account,
			@RequestParam("startYear") final Integer startYear
	) {
		model.addAttribute("startYear", startYear);
		model.addAttribute("allYears", reportsService.getAllYears(account));
		model.addAttribute("filteredYears", reportsService.getFilteredYears(account, startYear));

		List<SummaryRow> rows = reportsDao.getSummary(startYear, account);
		model.addAttribute("summary", rows);
		return "report_summary";
	}

	@GetMapping(value = "/balance_sheet")
	public String balanceSheet(
		Model model,
		@RequestParam("account") final Integer account
	) {
		model.addAttribute("account", account);
		return "balance_sheet";
	}

	@GetMapping(value = "/weekly")
	public String weekly(
			Model model,
			@RequestParam("account") final Integer account,
			@RequestParam("year") final Integer year,
			@RequestParam("month") final Integer month
	) {
		return "weekly";
	}
}
