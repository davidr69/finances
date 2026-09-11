package com.lavacro.finances.repositories.jpa;

import com.lavacro.finances.entities.AccountEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountsRepository extends JpaRepository<AccountEntity, Integer> {
	List<AccountEntity> findByActiveTrue(Sort sort);
}
