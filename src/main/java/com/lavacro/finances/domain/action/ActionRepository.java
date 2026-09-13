package com.lavacro.finances.domain.action;

import com.lavacro.finances.dto.ActionDTO;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ActionRepository extends ListCrudRepository<ActionDTO, Integer>, ListPagingAndSortingRepository<ActionDTO, Integer> {
}
