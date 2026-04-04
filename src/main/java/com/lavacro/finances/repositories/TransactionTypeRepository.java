package com.lavacro.finances.repositories;

import com.lavacro.finances.entities.TransactionTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionTypeEntity, Integer> {
}
