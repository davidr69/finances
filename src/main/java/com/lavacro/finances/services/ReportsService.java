package com.lavacro.finances.services;

import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ReportsService {
	private final DataSource datasource;

	@Language("SQL")
	private static final String YEARS_FOR_ACCOUNT = """
		SELECT DISTINCT CAST(date_part('year', mydate) AS INTEGER) AS year
		FROM action
		WHERE account = ?
		ORDER BY year
	""";

	@Language("SQL")
	private static final String FILTERED_YEARS = """
		SELECT DISTINCT CAST(DATE_PART('year', mydate) AS INTEGER) AS year
		FROM action
		WHERE account = ? AND DATE_PART('year', mydate) >= ?
		ORDER BY year
	""";

	ReportsService(DataSource dataSource) {
		this.datasource = dataSource;
	}

	public List<Integer> getAllYears(final Integer account) {
		log.info("getAllYears: {}", account);

		List<Integer> years = new ArrayList<>();
		try (
				Connection conn = datasource.getConnection();
				PreparedStatement stmt = conn.prepareStatement(YEARS_FOR_ACCOUNT)
		) {
			stmt.setInt(1, account);
			stmt.execute();
			while(stmt.getResultSet().next()) {
				years.add(stmt.getResultSet().getInt("year"));
			}
		} catch(SQLException e) {
			log.error("'getAllYears' error: {}", e.getMessage());
		}
		return years;
	}

	public List<Integer> getFilteredYears(final Integer account, final Integer start) {
		log.info("getFilteredYears: {}, {}", account, start);

		List<Integer> years = new ArrayList<>();
		try (
				Connection conn = datasource.getConnection();
				PreparedStatement stmt = conn.prepareStatement(FILTERED_YEARS)
		) {
			stmt.setInt(1, account);
			stmt.setInt(2, start);
			stmt.execute();
			while(stmt.getResultSet().next()) {
				years.add(stmt.getResultSet().getInt("year"));
			}
		} catch(SQLException e) {
			log.error("'getFilteredYears' error: {}", e.getMessage());
		}
		return years;
	}
}
