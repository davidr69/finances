package com.lavacro.finances.controllers;

import com.lavacro.finances.services.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Calendar;

@Controller
public class Cashbook {
	private static final Logger logger = LoggerFactory.getLogger(Cashbook.class);

	private final TransactionService transactionService;

	public Cashbook(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@GetMapping(value = "/cashbook")
	public String makePage(
			Model model,
			@RequestParam("account") Integer account,
			@RequestParam("year") Integer year,
			@RequestParam(value = "month", required = false) Integer month
	) {
		logger.info("Cashbook ...");
		if(month == null) {
			month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		}

		String[] months = {
				"All months", "January", "February", "March", "April", "May", "June",
				"July", "August", "September", "October", "November", "December"
		};

		model.addAttribute("transactions", transactionService.showItems(account, year, month));
		model.addAttribute("total", transactionService.getBalance(account));
		model.addAttribute("month", month);
		model.addAttribute("months", months);
		return "cashbook";
	}
}
