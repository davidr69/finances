package com.lavacro.finances.services;

import com.lavacro.finances.entities.EntityEntity;
import com.lavacro.finances.model.GenericResponse;
import com.lavacro.finances.repositories.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

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

	@Language("SQL")
	private static final String GET_ALL_ENTITIES = """
		SELECT id, acct, description, address, bank_alias, embedding
		FROM entities
		ORDER BY LOWER(description)
	""";

	@Language("SQL")
	private static final String UPDATE_RAG_SQL = """
		UPDATE entities
		SET bank_alias = ?
		WHERE id = ?
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
		entity.setValidated(entity.getEmbedding() != null);
		entity.setEmbedding(null);
		log.info("Returning: {}", entity);
		return entity;
	}

	public List<EntityEntity> getAllEntities() {
		List<EntityEntity> entities = new ArrayList<>();
		jdbcClient.sql(GET_ALL_ENTITIES).query(row -> {
			EntityEntity entity = new EntityEntity();
			entity.setId(row.getInt("id"));
			entity.setAccount(row.getString("acct"));
			entity.setDescription(row.getString("description"));
			entity.setAddress(row.getString("address"));
			entity.setAliases(row.getString("bank_alias"));
			String embedding = row.getString("embedding");
			entity.setValidated(embedding != null);
			entities.add(entity);
		});
		return entities;
	}

	public boolean updateRag(Integer id, String rag) {
		String cleanRag = rag.trim();
		try {
			jdbcClient.sql(UPDATE_RAG_SQL).params(cleanRag.length() == 0 ? null : cleanRag, id).update();
			return true;
		} catch(Exception e) {
			log.error("Error occurred while updating entity: {}", e.getMessage());
			return false;
		}
	}
}
