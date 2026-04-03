package com.lavacro.finances.services;

import com.lavacro.finances.dto.EntityDTO;
import com.lavacro.finances.dto.EntityTotalsDTO;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

	@Language("SQL")
	private static final String ENTITY_BY_YEAR = """
		SELECT amount, mydate, description, reference,
			SUM(amount) OVER(PARTITION BY entity ORDER BY description, mydate) AS total
		FROM action a
		JOIN entities e ON a.entity = e.id
		WHERE date_part('year', mydate) = ? AND account = ?
		ORDER BY description, mydate
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

	/*
		This data is for the entity report, whereby transactions for the year are grouped by entity
		with their amounts and eventually a total for the entity.
	 */
	public List<EntityTotalsDTO> byEntity(final Integer account, final Integer year) {
		log.info("byEntity: {}, {}", account, year);
		List<EntityDTO> tuples = new ArrayList<>();
		List<EntityTotalsDTO> totals = new ArrayList<>();

		try (
			Connection conn = datasource.getConnection();
			PreparedStatement stmt = conn.prepareStatement(ENTITY_BY_YEAR)
		) {
			String prevEntity = "";
			BigDecimal oldTotal = new BigDecimal(0);

			stmt.setInt(1, year);
			stmt.setInt(2, account);
			stmt.execute();
			while(stmt.getResultSet().next()) {
				ResultSet rs = stmt.getResultSet();

				EntityDTO tuple = new EntityDTO(
						rs.getBigDecimal("amount"),
						rs.getDate("mydate").toLocalDate(),
						rs.getString("description"),
						rs.getString("reference")
				);

				BigDecimal total = rs.getBigDecimal("total");

				if(!prevEntity.equals(tuple.description())) {
					if(!prevEntity.isEmpty()) {
						EntityTotalsDTO totalsDTO = new EntityTotalsDTO(prevEntity, oldTotal, tuples);
						totals.add(totalsDTO);
						tuples = new ArrayList<>();
					}
					prevEntity = tuple.description();
				}

				oldTotal = total;
				tuples.add(tuple);
			}
		} catch(SQLException e) {
			log.error("'byEntity' error: {}", e.getMessage());
		}
		return totals;
	}
}
