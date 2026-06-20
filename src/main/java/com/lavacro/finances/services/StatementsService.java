package com.lavacro.finances.services;

import com.lavacro.finances.dto.StatementDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatementsService {
	private final JdbcClient jdbcClient;
	private final JdbcTemplate jdbcTemplate; // for bulk writes

	@Language(value = "SQL")
	private static final String GET_STATEMENT_QUERY = """
		SELECT a.action_id, a.mydate, e.embedding IS NULL AS new_vendor, e.description AS vendor, a.amount, a.entity AS entity_id
		FROM staging.action a
		JOIN entities e ON a.entity = e.id
		WHERE account = :account
		ORDER BY a.statement_order
	""";

	@Language(value = "SQL")
	private static final String GET_STAGING_RECORD = """
		SELECT mydate, entity, amount
		FROM staging.action
		WHERE action_id = ?
	""";

	@Language(value = "SQL")
	private static final String MERGE_RECORDS = """
		INSERT INTO action (sequence, entity, account, amount, mydate, method, category)
		VALUES (NEXTVAL('action_seq'), ?, ?, ?, ?, 11, 0)
	""";

	@Language(value = "SQL")
	private static final String DELETE_FROM_STAGING = """
		DELETE FROM staging.action WHERE action_id = ?
	""";

	@Language(value = "SQL")
	private static final String UPDATE_ENTITY = "UPDATE staging.action SET entity = :entity WHERE action_id = :id";

	public List<StatementDTO> getStatement(Integer account) {
		List<StatementDTO> statements = new ArrayList<>();
		jdbcClient.sql(GET_STATEMENT_QUERY).param("account", account).query(rows -> {
			StatementDTO row = new StatementDTO(
				rows.getInt("action_id"),
				rows.getDate("mydate").toLocalDate(),
				rows.getBoolean("new_vendor"),
				rows.getInt("entity_id"),
				rows.getString("vendor"),
				rows.getBigDecimal("amount")
			);
			statements.add(row);
		});
		return statements;
	}

	public void mergeSelections(Map<Integer, Character> selections, int account) {
		// TODO: if transaction with new vendor is accepted, calculate the vectors
		List<Insert> insertions = new ArrayList<>();
		List<Integer> idsToDelete = new ArrayList<>();

		for(Map.Entry<Integer, Character> entry : selections.entrySet()) {
			Character selection = entry.getValue();
			Integer action_id = entry.getKey();

			if(selection == 'y') {
				Map<String, Object> row = jdbcClient.sql(GET_STAGING_RECORD).param(action_id).query().singleRow();
				insertions.add(new Insert(
					(Date) row.get("mydate"), (Integer) row.get("entity"), (BigDecimal) row.get("amount")
				));
			}
			idsToDelete.add(action_id);
		}

		log.info("insertions: {}", insertions);

		if(!insertions.isEmpty()) {
			jdbcTemplate.batchUpdate(MERGE_RECORDS, insertions, insertions.size(),
				(ps, item) -> {
					ps.setInt(1, item.entity());
					ps.setInt(2, account);
					ps.setBigDecimal(3, item.amount());
					ps.setDate(4, item.date());
				}
			);
		}

		if(!idsToDelete.isEmpty()) {
			jdbcTemplate.batchUpdate(DELETE_FROM_STAGING, idsToDelete, idsToDelete.size(),
				(ps, item) -> {
					ps.setInt(1, item);
				}
			);
		}
		log.info("Merged transactions");
	}

	public void updateEntity(Integer rowId, Integer entity) {
		jdbcClient.sql(UPDATE_ENTITY).param("entity", entity).param("id", rowId).update();
	}
}

record Insert (Date date, Integer entity, BigDecimal amount) { }
