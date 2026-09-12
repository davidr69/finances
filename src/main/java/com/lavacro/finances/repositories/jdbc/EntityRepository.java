package com.lavacro.finances.repositories.jdbc;

import com.lavacro.finances.dto.EntityDTO;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityRepository extends ListCrudRepository<EntityDTO, Integer>, ListPagingAndSortingRepository<EntityDTO, Integer> {
}
