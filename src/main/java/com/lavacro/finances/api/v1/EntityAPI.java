package com.lavacro.finances.api.v1;

import com.lavacro.finances.model.GenericResponse;
import com.lavacro.finances.services.EntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/entities")
@Slf4j
@RequiredArgsConstructor
public class EntityAPI {
	private final EntityService entityService;

	@DeleteMapping(value = "/{id}")
	public GenericResponse deleteEntity(@PathVariable Integer id) {
		return entityService.deleteEntity(id);
	}
}
