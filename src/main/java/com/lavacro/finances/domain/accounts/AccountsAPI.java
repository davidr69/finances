package com.lavacro.finances.domain.accounts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/accounts")
@Slf4j
@RequiredArgsConstructor
public class AccountsAPI {
	private final AccountsService accountsService;

	@GetMapping
	public List<AccountDTO> getAccounts() {
		return accountsService.findAllOrderByDescriptionAsc();
	}

}
