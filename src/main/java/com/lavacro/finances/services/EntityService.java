package com.lavacro.finances.services;

import com.lavacro.finances.dto.EntityDTO;
import com.lavacro.finances.model.GenericResponse;
import com.lavacro.finances.repositories.jdbc.EntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EntityService {
	private final EntityRepository entityRepository;
	private final JdbcClient jdbcClient;

	@Value("${finances.vector-reconcile.grace-seconds:60}")
	private int vectorReconcileGraceSeconds;

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

	@Language("SQL")
	private static final String STALE_VECTOR_IDS_SQL = """
		SELECT id
		FROM entities
		WHERE rag_updated IS NOT NULL
		  AND (vector_sync IS NULL OR vector_sync < rag_updated)
		  AND rag_updated < NOW() - MAKE_INTERVAL(secs => ?)
	""";

	public GenericResponse deleteEntity(Integer id) {
		GenericResponse resp = new GenericResponse();
		try {
			entityRepository.deleteById(id);
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

	public EntityDTO getEntity(Integer id) {
		EntityDTO entity = entityRepository.findById(id).orElse(null);
		if(entity == null) {
			log.error("Entity not found with id: {}", id);
			return null;
		}
		entity.setValidated(entity.getEmbedding() != null);
		entity.setEmbedding(null);
		log.info("Returning: {}", entity);
		return entity;
	}

	public List<EntityDTO> getAllEntities() {
		return jdbcClient.sql(GET_ALL_ENTITIES).query(EntityDTO.class).list();
	}

	public List<Integer> findEntitiesNeedingVectorSync() {
		return jdbcClient.sql(STALE_VECTOR_IDS_SQL)
			.param(vectorReconcileGraceSeconds)
			.query(Integer.class)
			.list();
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
