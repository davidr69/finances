package com.lavacro.finances.api.v1;

import com.lavacro.finances.entities.EntityEntity;
import com.lavacro.finances.kafka.service.DecisionService;
import com.lavacro.finances.model.GenericResponse;
import com.lavacro.finances.services.EntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/entities")
@Slf4j
@RequiredArgsConstructor
public class EntityAPI {
	private final EntityService entityService;
	private final DecisionService decisionService;

	@DeleteMapping(value = "/{id}")
	public GenericResponse deleteEntity(@PathVariable Integer id) {
		return entityService.deleteEntity(id);
	}

	@GetMapping(value = "/{id}")
	public EntityEntity getEntity(@PathVariable Integer id) {
		log.info("Get entity id: {}", id);
		return entityService.getEntity(id);
	}

	@PutMapping(value = "/accept/{id}")
	public GenericResponse acceptEntity(@PathVariable Integer id) {
		log.info("Accepting entity: {}", id);
		GenericResponse resp = new GenericResponse();
		try {
			decisionService.generateVector(id);
			resp.setCode(0);
			resp.setMessage("Entity accepted successfully");
		} catch (Exception e) {
			log.error("Error occurred while accepting entity: {}", e.getMessage());
			resp.setCode(1);
			resp.setMessage("Unable to accept entity");
		}
		return resp;
	}
}
