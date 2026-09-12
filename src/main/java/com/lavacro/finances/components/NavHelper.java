package com.lavacro.finances.components;

import com.lavacro.finances.dto.AccountDTO;
import com.lavacro.finances.services.AccountsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("navHelper")
@RequiredArgsConstructor
public class NavHelper {
	private final AccountsService accountsService;

	public List<AccountDTO> getAccounts() {
		return accountsService.findAllOrderByDescriptionAsc();
	}
}
