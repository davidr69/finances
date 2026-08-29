package com.lavacro.finances.api.v1;

import com.lavacro.finances.entities.EntityEntity;
import com.lavacro.finances.entities.AccountEntity;
import com.lavacro.finances.entities.CategoryEntity;

import com.lavacro.finances.entities.TransactionTypeEntity;
import com.lavacro.finances.repositories.CategoryRepository;
import com.lavacro.finances.services.AccountsService;
import com.lavacro.finances.services.EntityService;
import com.lavacro.finances.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/select")
@RequiredArgsConstructor
public class DropDownsAPI {
	private final EntityService entityService;
	private final CategoryRepository categoryRepository;
	private final TransactionService transactionService;
	private final AccountsService accountsService;

	@GetMapping(value = "/merchants")
	public List<EntityEntity> getMerchants() {
		return entityService.getAllEntities();
	}

	@GetMapping(value = "/transaction_types")
	public List<TransactionTypeEntity> getTransactionTypes() {
		return transactionService.findAllOrderByDescriptionAsc();
	}

	@GetMapping(value = "/accounts")
	public List<AccountEntity> getAccounts() {
		return accountsService.findAllOrderByDescriptionAsc();
	}

	@GetMapping(value = "/categories")
	public List<CategoryEntity> getCategories() {
		return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "description"));
	}
}
