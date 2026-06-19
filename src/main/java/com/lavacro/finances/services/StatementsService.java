package com.lavacro.finances.services;

import com.lavacro.finances.dto.StatementDTO;
import com.lavacro.finances.entities.ActionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatementsService {
	private final JdbcClient jdbcClient;

	@Language(value = "SQL")
	private static final String GET_STATEMENT_QUERY = """
		SELECT a.action_id, a.mydate, e.embedding IS NULL AS new_vendor, e.description AS vendor, a.amount
		FROM staging.action a
		JOIN entities e ON a.entity = e.id
		WHERE account = :account
		ORDER BY a.statement_order
	""";

	@Language(value = "SQL")
	private static final String GET_STAGING_RECORD = """
		SELECT mydate, entity, amount
		FROM staging.action
		WHERE action_id = :action_id
	""";

	public List<StatementDTO> getStatement(Integer account) {
		List<StatementDTO> statements = new ArrayList<>();
		jdbcClient.sql(GET_STATEMENT_QUERY).param("account", account).query(rows -> {
			StatementDTO row = new StatementDTO(
				rows.getInt("action_id"),
				rows.getDate("mydate").toLocalDate(),
				rows.getBoolean("new_vendor"),
				rows.getString("vendor"),
				rows.getBigDecimal("amount")
			);
			statements.add(row);
		});
		return statements;
	}

	public void mergeSelections(Map<Integer, Character> selections) {
		for(Map.Entry<Integer, Character> entry : selections.entrySet()) {
			Character selection = entry.getValue();
			Integer action_id = entry.getKey();

			if(selection == 'y') {
				JdbcClient.ResultQuerySpec res = jdbcClient.sql(GET_STAGING_RECORD).param(action_id).query();
//				ActionEntity actionEntity = new ActionEntity();
//				actionEntity.
			} else {

			}
		}
	}
}
