package com.lavacro.finances.api.v1;

import com.lavacro.finances.dto.AccountDTO;
import com.lavacro.finances.dto.CategoryDTO;
import com.lavacro.finances.dto.EntityDTO;

import com.lavacro.finances.dto.TransactionTypeDTO;
import com.lavacro.finances.services.AccountsService;
import com.lavacro.finances.services.CategoryService;
import com.lavacro.finances.services.EntityService;
import com.lavacro.finances.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/select")
@RequiredArgsConstructor
public class DropDownsAPI {
	private final EntityService entityService;
	private final CategoryService categoryService;
	private final TransactionService transactionService;
	private final AccountsService accountsService;

	@GetMapping(value = "/merchants")
	public List<EntityDTO> getMerchants() {
		return entityService.getAllEntities();
	}

	@GetMapping(value = "/transaction_types")
	public List<TransactionTypeDTO> getTransactionTypes() {
		return transactionService.findAllOrderByDescriptionAsc();
	}

	@GetMapping(value = "/accounts")
	public List<AccountDTO> getAccounts() {
		return accountsService.findAllOrderByDescriptionAsc();
	}

	@GetMapping(value = "/categories")
	public List<CategoryDTO> getCategories() {
		return categoryService.findAllOrderByDescriptionAsc();
	}
}
