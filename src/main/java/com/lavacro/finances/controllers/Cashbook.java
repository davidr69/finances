package com.lavacro.finances.controllers;

import com.lavacro.finances.domain.action.ActionService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Calendar;

@Controller
@Slf4j
public class Cashbook {
	private final ActionService actionService;

	public Cashbook(ActionService actionService) {
		this.actionService = actionService;
	}

	@GetMapping(value = "/cashbook")
	public String makePage(
			Model model,
			@RequestParam("account") Integer account,
			@RequestParam("year") Integer year,
			@RequestParam(value = "month", required = false) Integer month
	) {
		log.info("Cashbook ...");
		if(month == null) {
			month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		}

		String[] months = {
				"All months", "January", "February", "March", "April", "May", "June",
				"July", "August", "September", "October", "November", "December"
		};

		model.addAttribute("transactions", actionService.showItems(account, year, month));
		model.addAttribute("total", actionService.getBalance(account));
		model.addAttribute("month", month);
		model.addAttribute("months", months);
		return "cashbook";
	}
}
