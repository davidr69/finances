package com.lavacro.finances.services;

import com.lavacro.finances.entities.AccountEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountsService {
	private final AccountRepository accountRepository;

	AccountsService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	public List<AccountEntity> findAllOrderByDescriptionAsc() {
		return accountRepository.findByActiveTrue(Sort.by(Sort.Direction.ASC, "description"));
	}
}

@Repository
interface AccountRepository extends JpaRepository<AccountEntity, Integer> {
    List<AccountEntity> findByActiveTrue(Sort sort);
}
