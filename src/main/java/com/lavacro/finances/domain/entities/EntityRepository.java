package com.lavacro.finances.domain.entities;

import com.lavacro.finances.dto.EntityDTO;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
interface EntityRepository extends ListCrudRepository<EntityDTO, Integer>, ListPagingAndSortingRepository<EntityDTO, Integer> {
}
