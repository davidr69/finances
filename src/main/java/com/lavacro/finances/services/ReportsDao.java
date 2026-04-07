package com.lavacro.finances.services;

import com.lavacro.finances.entities.EntitySummary;
import com.lavacro.finances.model.reports.SummaryRow;

import com.lavacro.finances.repositories.SummaryRepository;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class ReportsDao {
	private final SummaryRepository summaryRepository;
	private final JdbcClient jdbcClient;

	public ReportsDao(
			SummaryRepository summaryRepository,
			JdbcClient jdbcClient
	) {
		this.summaryRepository = summaryRepository;
		this.jdbcClient = jdbcClient;
	}

	public List<SummaryRow> getSummary(final Integer startYear, final Integer account) {
		log.info("getSummary: {}, {}", startYear, account);

		final List<EntitySummary> entitySummaryList = summaryRepository.getSummaries(account, startYear);
		final List<SummaryRow> rows = new ArrayList<>();
		String entity = "";
		SummaryRow row = null;
		BigDecimal total = new BigDecimal(0);
		final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

		for(EntitySummary item: entitySummaryList) {
			String dbVendor = item.getEntity();
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
			row.getColumns().put(item.getTheyear(), item.getMoney().trim());
			total = total.add(item.getAmount());
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
