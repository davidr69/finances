package com.lavacro.finances.controllers;

import com.lavacro.finances.services.AccountsService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Home {
	private static final Logger log = LoggerFactory.getLogger(Home.class);

	private final AccountsService accountsService;
	private final HttpSession session;

	public Home(AccountsService accountsService, HttpSession session) {
		this.accountsService = accountsService;
		this.session = session;
	}

	@GetMapping(value = {"/", "/nav"})
	public String showNav(Model model) {
		log.info("Nav frame");
		model.addAttribute("accounts", accountsService.findAllOrderByDescriptionAsc());
		return "nav";
	}

	@GetMapping(value = "/logout")
	public String logout(HttpServletResponse httpResp) {
		session.invalidate();
		log.info("Destroyed session");
		return "redirect:/login.html";
	}
}
