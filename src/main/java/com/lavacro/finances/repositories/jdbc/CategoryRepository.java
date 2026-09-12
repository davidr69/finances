package com.lavacro.finances.repositories.jdbc;

import com.lavacro.finances.dto.CategoryDTO;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends ListPagingAndSortingRepository<CategoryDTO, Integer> { }
