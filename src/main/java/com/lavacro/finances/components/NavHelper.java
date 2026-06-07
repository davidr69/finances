package com.lavacro.finances.components;

import com.lavacro.finances.entities.AccountEntity;
import com.lavacro.finances.services.AccountsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("navHelper")
public class NavHelper {
	private final AccountsService accountsService;

	public NavHelper(AccountsService accountsService) {
		this.accountsService = accountsService;
	}

	public List<AccountEntity> getAccounts() {
		return accountsService.findAllOrderByDescriptionAsc();
	}
}
