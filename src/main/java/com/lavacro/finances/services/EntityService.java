package com.lavacro.finances.services;

import com.lavacro.finances.entities.EntityEntity;
import com.lavacro.finances.model.GenericResponse;
import com.lavacro.finances.repositories.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EntityService {
	private final MerchantRepository merchantRepository;
	private final JdbcClient jdbcClient;

	@Language("SQL")
	private static final String INSERT_ENTITY_SQL = """
		INSERT INTO entities (acct, description, address)
		VALUES (?, ?, ?)
	""";

	public GenericResponse deleteEntity(Integer id) {
		GenericResponse resp = new GenericResponse();
		try {
			merchantRepository.deleteById(id);
			resp.setCode(0);
			log.info("Entity deleted successfully");
		} catch (Exception e) {
			log.error("Error occurred while deleting entity: {}", e.getMessage());
			resp.setCode(1);
			resp.setMessage("Unable to delete entity");
		}
		return resp;
	}

	public void createEntity(String description, String account, String address) {
		jdbcClient.sql(INSERT_ENTITY_SQL)
			.params(account, description, address)
			.update();
	}

	public EntityEntity getEntity(Integer id) {
		EntityEntity entity = merchantRepository.findById(id).orElse(null);
		if(entity == null) {
			log.error("Entity not found with id: {}", id);
			return null;
		}
		entity.setEmbedding(null);
		entity.setValidated(entity.getEmbedding() != null);
		log.info("Returning: {}", entity);
		return entity;
	}
}
