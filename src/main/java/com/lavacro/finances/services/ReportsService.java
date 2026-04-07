package com.lavacro.finances.services;

import com.lavacro.finances.dto.BalanceDTO;
import com.lavacro.finances.dto.EntityDTO;
import com.lavacro.finances.dto.EntityTotalsDTO;
import com.lavacro.finances.dto.EntitySummaryDTO;
import com.lavacro.finances.model.reports.BalanceSheet;
import com.lavacro.finances.model.reports.SummaryRow;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class ReportsService {
	private final JdbcClient jdbcClient;

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

	@Language(value = "SQL")
	private static final String BALANCE_QUERY = """
		WITH qry AS (
			SELECT
				SUM(CASE WHEN amount < 0 THEN amount ELSE 0 END) AS withdrawals,
				SUM(CASE WHEN amount >=0 THEN amount ELSE 0 END) AS deposits,
				DATE_PART('year', mydate) AS yr, DATE_PART('month', mydate) AS mo
			FROM action
			WHERE account = ?
			GROUP BY yr, mo
		)
		SELECT withdrawals, deposits, deposits + withdrawals AS surp_def, yr, mo
		FROM qry
		ORDER BY yr, mo
	""";

	@Language(value = "SQL")
	private static final String GET_SUMMARIES = """
		SELECT e.description AS entity, SUM(a.amount) AS amount,
			TO_CHAR(SUM(a.amount), '999,999,999D99') AS money,
			CAST(DATE_PART('year', a.mydate) AS INTEGER) AS theyear
		FROM action a
		JOIN entities e ON a.entity = e.id
		WHERE a.account = ? AND DATE_PART('year', a.mydate) >= ?
		GROUP BY description, theyear
	""";

	ReportsService(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public List<Integer> getAllYears(final Integer account) {
		log.info("getAllYears: {}", account);

		return jdbcClient.sql(YEARS_FOR_ACCOUNT).params(account).query(Integer.class).list();
	}

	public List<Integer> getFilteredYears(final Integer account, final Integer start) {
		log.info("getFilteredYears: {}, {}", account, start);

		return jdbcClient.sql(FILTERED_YEARS).params(account, start).query(Integer.class).list();
	}

	/*
		This data is for the entity report, whereby transactions for the year are grouped by entity
		with their amounts and eventually a total for the entity.
	 */
	public List<EntityTotalsDTO> byEntity(final Integer account, final Integer year) {
		log.info("byEntity: {}, {}", account, year);
		List<EntityDTO> tuples = new ArrayList<>();
		List<EntityTotalsDTO> totals = new ArrayList<>();

		// make these arrays as a hack to get around being unable to update them within the lambda
		String[] prevEntity = {""};
		BigDecimal[] oldTotal = {new BigDecimal(0)};

		jdbcClient.sql(ENTITY_BY_YEAR).params(year, account).query(row -> {
			EntityDTO tuple = new EntityDTO(
					row.getBigDecimal("amount"),
					row.getDate("mydate").toLocalDate(),
					row.getString("description"),
					row.getString("reference")
			);

			BigDecimal total = row.getBigDecimal("total");

			if(!prevEntity[0].equals(tuple.description())) {
				if(!prevEntity[0].isEmpty()) {
					EntityTotalsDTO totalsDTO = new EntityTotalsDTO(prevEntity[0], oldTotal[0], tuples);
					totals.add(totalsDTO);
					tuples.clear();
				}
				prevEntity[0] = tuple.description();
			}

			oldTotal[0] = total;
			tuples.add(tuple);
		});

		totals.add(new EntityTotalsDTO(prevEntity[0], oldTotal[0], tuples));

		return totals;
	}

	public BalanceSheet balanceSheet(final Integer account) {
		log.info("balance sheet");
		BalanceSheet bs = new BalanceSheet();
		bs.setBalanceList(new HashMap<>());

		final BigDecimal[] runningTotal = {new BigDecimal(0)};
		final Integer[] prevYear = {0};
		final List<BalanceDTO> yearList = new ArrayList<>();

		jdbcClient.sql(BALANCE_QUERY).params(account).query(row -> {
			BigDecimal withdrawals = row.getBigDecimal("withdrawals");
			BigDecimal deposits = row.getBigDecimal("deposits");
			BigDecimal surplusDeficit = row.getBigDecimal("surp_def");
			int year = row.getInt("yr");
			int month = row.getInt("mo");

			if(prevYear[0] != year ) {
				if(prevYear[0] != 0) {
					bs.getBalanceList().put(prevYear[0], yearList.stream().toList());
				}
				prevYear[0] = year;
				yearList.clear();
			}

			runningTotal[0] = runningTotal[0].add(surplusDeficit);

			BalanceDTO bal = new BalanceDTO(month, deposits, withdrawals, surplusDeficit, runningTotal[0]);
			yearList.add(bal);
		});

		bs.getBalanceList().put(prevYear[0], yearList);
		bs.setCode(0);

		return bs;
	}

	public List<SummaryRow> getSummary(final Integer startYear, final Integer account) {
		log.info("getSummary: {}, {}", startYear, account);

		List<EntitySummaryDTO> entitySummaryDTOList = jdbcClient.sql(GET_SUMMARIES).params(account, startYear).query(EntitySummaryDTO.class).list();
		List<SummaryRow> rows = new ArrayList<>();
		String entity = "";
		SummaryRow row = null;
		BigDecimal total = new BigDecimal(0);
		NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

		for(EntitySummaryDTO item: entitySummaryDTOList) {
			String dbVendor = item.entity();
			if(!dbVendor.equals(entity)) {
				if(!entity.isEmpty()) {
					row.setTotal(total);
					row.setFormattedTotal(currencyFormatter.format(total.doubleValue()));
					rows.add(row);
				}

				total = new BigDecimal(0);
				entity = dbVendor;
				row = new SummaryRow();
				row.setEntity(entity);
				row.setColumns(new HashMap<>());
			}
			assert row != null;
			row.getColumns().put(item.theyear(), item.money().trim());
			total = total.add(item.amount());
		}

		if(row == null) {
			return rows;
		}
		row.setTotal(total);
		row.setFormattedTotal(currencyFormatter.format(total.doubleValue()));
		rows.add(row);

		Collections.sort(rows);
		AtomicReference<Integer> rank = new AtomicReference<>(1);
		rows.forEach( unranked -> unranked.setRank(rank.getAndSet(rank.get() + 1)));
		return rows;
	}
}
