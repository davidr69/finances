package com.lavacro.finances.services;

import com.lavacro.finances.dto.StatementDTO;
import com.lavacro.finances.kafka.service.DecisionService;
import com.lavacro.finances.shared.proto.DecisionProto;
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
	private final DecisionService decisionService;

	private static final String USE_VECTOR = "vector";
	private static final String USE_LLM = "llm";
	private static final String REJECT = "reject";

	@Language(value = "SQL")
	private static final String GET_STATEMENT_QUERY = """
		SELECT
			a.action_id,
			a.mydate,
			a.amount,
			a.description AS statement_vendor,
			a.entity AS vector_id,
			CASE WHEN a.entity = a.llm_entity
				THEN NULL
				ELSE a.llm_entity
			END AS llm_id,
			e.description AS vector_vendor,
			CASE WHEN a.entity = a.llm_entity
				THEN NULL
				ELSE l.description
			END AS llm_vendor,
			(a.llm_entity IS NOT NULL AND l.embedding IS NULL) AS new_entity
		FROM staging.action a
		JOIN entities e ON a.entity = e.id
		LEFT JOIN entities l ON a.llm_entity = l.id
		WHERE account = :account
		ORDER BY a.statement_order;
	""";

	@Language(value = "SQL")
	private static final String GET_STAGING_RECORD = """
		SELECT a.mydate, a.entity, a.llm_entity, a.amount,
			CASE
				WHEN a.llm_entity IS NOT NULL
					THEN e.embedding IS NULL
				END
			AS new_entity
		FROM staging.action a
		LEFT JOIN entities e ON a.llm_entity = e.id
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
	private static final String UPDATE_ENTITY = "UPDATE staging.action SET llm_entity = :entity WHERE action_id = :id";

	public List<StatementDTO> getStatement(Integer account) {
		List<StatementDTO> statements = new ArrayList<>();
		jdbcClient.sql(GET_STATEMENT_QUERY).param("account", account).query(rows -> {
			int vectorId = rows.getInt("vector_id");
			int llmId = rows.getInt("llm_id");
			StatementDTO row = new StatementDTO(
				rows.getInt("action_id"),
				rows.getDate("mydate").toLocalDate(),
				rows.getBigDecimal("amount"),
				rows.getString("statement_vendor"),
				vectorId,
				vectorId == llmId ? null : llmId,
				rows.getString("vector_vendor"),
				vectorId == llmId ? null : rows.getString("llm_vendor"),
				rows.getBoolean("new_entity")
			);
			statements.add(row);
		});
		return statements;
	}

	public void mergeSelections(Map<Integer, String> selections, int account) {
		List<Insert> insertions = new ArrayList<>();
		List<Integer> idsToDelete = new ArrayList<>();

		for(Map.Entry<Integer, String> entry : selections.entrySet()) {
			String selection = entry.getValue();
			Integer actionId = entry.getKey();

			if(USE_VECTOR.equals(selection) || USE_LLM.equals(selection)) {
				Map<String, Object> row = jdbcClient.sql(GET_STAGING_RECORD).param(actionId).query().singleRow();
				insertions.add(new Insert(
					(Date) row.get("mydate"),
					USE_VECTOR.equals(selection) ? (Integer) row.get("entity") : (Integer) row.get("llm_entity"),
					(BigDecimal) row.get("amount")
				));

				if(USE_LLM.equals(selection) && (boolean) row.get("new_entity")) {
					// calculate the vector
					DecisionProto.DecisionMessage message = DecisionProto.DecisionMessage.newBuilder()
						.setDecision(DecisionProto.DecisionMessage.Decision.USE_LLM)
						.setTransactionId(actionId)
						.build();
					// right now, don't need the other fields in the protobuf message
					decisionService.send(message);
				}
			}
			// implicitly reject
			idsToDelete.add(actionId);
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
