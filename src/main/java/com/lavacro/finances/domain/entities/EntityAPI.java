package com.lavacro.finances.domain.entities;

import com.lavacro.finances.dto.EntityDTO;
import com.lavacro.finances.kafka.service.DecisionService;
import com.lavacro.finances.model.GenericResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/entities")
@Slf4j
@RequiredArgsConstructor
public class EntityAPI {
	private final EntityService entityService;
	private final DecisionService decisionService;

	@DeleteMapping(value = "/{id}")
	GenericResponse deleteEntity(@PathVariable Integer id) {
		return entityService.deleteEntity(id);
	}

	@GetMapping(value = "/{id}")
	EntityDTO getEntity(@PathVariable Integer id) {
		log.info("Get entity id: {}", id);
		return entityService.getEntity(id);
	}

	@PutMapping(value = "/accept/{id}")
	GenericResponse acceptEntity(@PathVariable Integer id) {
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

	@PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	GenericResponse updateEntity(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
		GenericResponse resp = new GenericResponse();
		String rag = (String) body.get("rag");
		if(rag == null) {
			resp.setCode(1);
			resp.setMessage("RAG is required");
			return resp;
		}
		entityService.updateRag(id, rag);
		decisionService.generateVector(id);
		resp.setCode(0);
		resp.setMessage("Entity updated successfully");
		return resp;
	}

	@GetMapping()
	List<EntityDTO> getActiveEntities() {
		return entityService.getAllEntities().stream()
			.filter(entity -> entity.getEmbedding() != null)
			.toList();
	}

}
