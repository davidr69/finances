package com.lavacro.finances.repositories.jdbc;

import com.lavacro.finances.dto.TransactionTypeDTO;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionTypeRepository extends ListCrudRepository<TransactionTypeDTO, Integer>, ListPagingAndSortingRepository<TransactionTypeDTO, Integer> {
}
