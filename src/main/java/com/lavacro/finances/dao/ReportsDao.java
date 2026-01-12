package com.lavacro.finances.dao;

import com.lavacro.finances.entities.BalanceSheetEntity;
import com.lavacro.finances.entities.EntityByYear;
import com.lavacro.finances.entities.EntitySummary;
import com.lavacro.finances.model.reports.EntityObj;
import com.lavacro.finances.model.reports.Transaction;
import com.lavacro.finances.model.reports.SummaryRow;
import com.lavacro.finances.model.reports.BalanceSheet;
import com.lavacro.finances.model.reports.Balance;

import com.lavacro.finances.repositories.BalanceSheetRepository;
import com.lavacro.finances.repositories.EntityRepository;
import com.lavacro.finances.repositories.SummaryRepository;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ReportsDao {
	private static final Logger logger = LoggerFactory.getLogger(ReportsDao.class);

	private final BalanceSheetRepository balanceSheetRepository;
	private final EntityRepository entityRepository;
	private final SummaryRepository summaryRepository;

	public ReportsDao(BalanceSheetRepository balanceSheetRepository,
					  EntityRepository entityRepository,
					  SummaryRepository summaryRepository
	) {
		this.balanceSheetRepository = balanceSheetRepository;
		this.entityRepository = entityRepository;
		this.summaryRepository = summaryRepository;
	}

	public List<EntityObj> byEntity(final Integer account, final Integer year) {
		logger.info("byEntity: {}, {}", account, year);
		List<EntityByYear> tuples = entityRepository.byEntity(year, account);

		final List<EntityObj> resp = new ArrayList<>();
		EntityObj entity = null;
		BigDecimal total = new BigDecimal(0);
		String prevEntity = "";

		try {
			for(EntityByYear tuple: tuples) {
				String desc = tuple.getDescription();
				if(!prevEntity.equals(desc)) {
					if(!prevEntity.isEmpty()) {
						entity.setTotal(total);
						resp.add(entity);
					}

					entity = new EntityObj();
					entity.setName(desc);
					entity.setTransactions(new ArrayList<>());
					prevEntity = desc;
				}

				String reference = tuple.getReference();
				Transaction transaction = new Transaction(
						tuple.getMydate(), tuple.getAmount(), reference == null ? "" : reference
				);
				assert entity != null;
				entity.getTransactions().add(transaction);
				total = tuple.getTotal();
			}
		} catch(Exception e) {
			logger.error(e.getMessage());
		}

		if(entity == null) {
			return resp;
		}
		entity.setTotal(total);
		resp.add(entity);
		return resp;
	}

	public List<SummaryRow> getSummary(final Integer startYear, final Integer account) {
		logger.info("getSummary: {}, {}", startYear, account);

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

	public BalanceSheet balanceSheet(final Integer account) {
		logger.info("balance sheet");
		BalanceSheet bs = new BalanceSheet();
		bs.setBalanceList(new HashMap<>());

		BigDecimal runningTotal = new BigDecimal(0);

		int yr = 0;
		List<Balance> yearList = new ArrayList<>();

		try {
			List<BalanceSheetEntity> balances = balanceSheetRepository.balanceSheet(account);

			for(BalanceSheetEntity tuple: balances) {
				if(tuple.getYear() != yr) {
					if(yr != 0) {
						bs.getBalanceList().put(yr, yearList);
					}
					yr = tuple.getYear();
					yearList = new ArrayList<>();
				}
				BigDecimal surplusDeficit = tuple.getSurplusOrDeficit();
				runningTotal = runningTotal.add(surplusDeficit);

				Balance bal = new Balance(
						tuple.getMonth(), tuple.getDeposits(), tuple.getWithdrawals(), surplusDeficit, runningTotal
				);

				yearList.add(bal);
			}
			bs.getBalanceList().put(yr, yearList);

			bs.setCode(0);
		} catch(Exception e) {
			logger.error(e.getMessage());
			bs.setCode(1);
			bs.setMessage(e.getMessage());
		}
		return bs;
	}
}
