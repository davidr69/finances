package com.lavacro.finances.services;

import com.lavacro.finances.dto.AccountDTO;
import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountsService {
	private final JdbcClient jdbcClient;

	@Language("SQL")
	private static final String GET_ALL_ACCOUNTS = """
		SELECT a.id, a.number AS account, a.description, t.action
		FROM accounts a
		JOIN account_type t ON a.type = t.id
		WHERE a.active = true
		ORDER BY a.description
	""";

	public List<AccountDTO> findAllOrderByDescriptionAsc() {
		return jdbcClient.sql(GET_ALL_ACCOUNTS).query(AccountDTO.class).list();
	}
}
