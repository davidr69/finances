package com.lavacro.finances.services;

import com.lavacro.finances.model.GenericResponse;
import com.lavacro.finances.repositories.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

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

	public GenericResponse deleteEntity(@PathVariable Integer id) {
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
}
