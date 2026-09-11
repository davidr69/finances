package com.lavacro.finances.repositories.jdbc;

import com.lavacro.finances.entities.CategoryEntity;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends ListPagingAndSortingRepository<CategoryEntity, Integer> { }
