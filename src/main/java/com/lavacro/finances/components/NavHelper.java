package com.lavacro.finances.components;

import com.lavacro.finances.entities.AccountEntity;
import com.lavacro.finances.services.AccountsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("navHelper")
@RequiredArgsConstructor
public class NavHelper {
	private final AccountsService accountsService;

	public List<AccountEntity> getAccounts() {
		return accountsService.findAllOrderByDescriptionAsc();
	}
}
