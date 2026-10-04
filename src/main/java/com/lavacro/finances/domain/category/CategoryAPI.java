package com.lavacro.finances.domain.category;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryAPI {
	private final CategoryService categoryService;

	@GetMapping
	public List<CategoryDTO> getCategories() {
		return categoryService.findAllOrderByDescriptionAsc();
	}
}
