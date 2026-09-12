package com.lavacro.finances.services;

import com.lavacro.finances.dto.CategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
	private final JdbcClient jdbcClient;

	private static final String GET_ALL_CATEGORIES = "SELECT id, description FROM categories ORDER BY description";

	public List<CategoryDTO> findAllOrderByDescriptionAsc() {
		return jdbcClient.sql(GET_ALL_CATEGORIES).query(CategoryDTO.class).list();
	}
}
