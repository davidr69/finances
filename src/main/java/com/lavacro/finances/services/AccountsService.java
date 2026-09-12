package com.lavacro.finances.services;

import com.lavacro.finances.entities.AccountEntity;
import com.lavacro.finances.repositories.jpa.AccountsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountsService {
	private final AccountsRepository accountsRepository;

	public List<AccountEntity> findAllOrderByDescriptionAsc() {
		return accountsRepository.findByActiveTrue(Sort.by(Sort.Direction.ASC, "description"));
	}
}
