package com.lavacro.finances.services;

import com.lavacro.finances.entities.AccountEntity;
import com.lavacro.finances.repositories.AccountsRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountsService {
	private final AccountsRepository accountsRepository;

	AccountsService(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
	}

	public List<AccountEntity> findAllOrderByDescriptionAsc() {
		return accountsRepository.findByActiveTrue(Sort.by(Sort.Direction.ASC, "description"));
	}
}
