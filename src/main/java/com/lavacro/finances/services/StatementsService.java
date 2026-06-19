package com.lavacro.finances.services;

import com.lavacro.finances.dto.StatementDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
}
