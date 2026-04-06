package com.lavacro.finances.services;

import com.lavacro.finances.repositories.ActionRepository;
import com.lavacro.finances.dto.TransactionDTO;
import com.lavacro.finances.entities.ActionEntity;
import com.lavacro.finances.entities.TransactionTypeEntity;
import com.lavacro.finances.model.*;

import com.lavacro.finances.repositories.TransactionTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TransactionService {
	private final TransactionTypeRepository transactionTypeRepository;
	private final ActionRepository actionRepository;
	private final DataSource dataSource;
	private final NumberFormat nf = NumberFormat.getInstance();

	@Language("SQL")
	private static final String SUM_UP_TO_DATE = """
		SELECT SUM(amount) AS balance
		FROM action
		WHERE account = ? AND visible = 't' AND mydate < ?
	""";

	@Language("SQL")
	private static final String SUM_FOR_ACCOUNT = """
		SELECT SUM(amount) AS balance
		FROM action
		WHERE account = ? AND reconciled='t'
	""";

	@Language("SQL")
	private static final String ONE_ACCOUNT_WITHIN_DATE = """
		SELECT act.sequence, act.amount, act.mydate, act.reference, act.reconciled, act.visible,
			e.description AS entity, trn.description AS method
		FROM action act
		JOIN entities e ON act.entity = e.id
		JOIN trans_type trn on act.method = trn.id
		WHERE act.account = ? AND act.mydate BETWEEN ? AND ?
		ORDER BY mydate, amount DESC, e.description
	""";

	TransactionService(
			TransactionTypeRepository transactionTypeRepository,
			ActionRepository actionRepository,
			DataSource dataSource
	) {
		this.transactionTypeRepository = transactionTypeRepository;
		this.actionRepository = actionRepository;
		this.dataSource = dataSource;
		nf.setMinimumFractionDigits(2);
	}

	public List<TransactionTypeEntity> findAllOrderByDescriptionAsc() {
		return transactionTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "description"));
	}

	public ActionResponse persistTransaction(NewTransaction req, String plusOrMinus) {
		ActionResponse resp = new ActionResponse();
		float howMuch;
		try {
			howMuch = Float.parseFloat(req.getAmount());
		} catch(NumberFormatException e) {
			log.error(e.getMessage());
			resp.setCode(1);
			resp.setMessage(e.getMessage());
			return resp;
		}
		BigDecimal amount = BigDecimal.valueOf(plusOrMinus.equals("-") ? -howMuch : howMuch);

		ActionEntity actionEntity = new ActionEntity();
		actionEntity.setEntity(req.getEntity());
		actionEntity.setAccount(req.getAccount());
		actionEntity.setAmount(amount);
		actionEntity.setMydate(LocalDate.of(req.getYear(), req.getMonth(), req.getDay()));
		actionEntity.setMethod(req.getMethod());
		if(!req.getReference().isEmpty()) {
			actionEntity.setReference(req.getReference());
		}
		if(req.getCategory() != null) {
			actionEntity.setCategory(req.getCategory());
		}
		actionRepository.save(actionEntity);
		resp.setCode(0);
		resp.setMessage("Successfully added");
		return resp;
	}

	public void deleteTransaction(final Integer id) {
		actionRepository.deleteById(id);
	}

	public ActionEntity findOne(final Integer id) {
		return actionRepository.findById(id).orElse(null);
	}

	public void updateTransaction(final ActionEntity actionEntity) {
		if(actionEntity.getReference().isBlank()) {
			actionEntity.setReference(null);
		}
		actionRepository.save(actionEntity);
	}

	public void newTransaction(final NewTransaction newTransaction) {
		TransactionTypeEntity ttype =  transactionTypeRepository.findById(newTransaction.getMethod()).orElse(null);
		if(ttype != null) {
			ActionResponse resp = persistTransaction(newTransaction, ttype.getCreditDebit());
			if(resp.getCode() != 0) {
				log.error("Could not add transaction");
			}
		} else {
			log.error("Cannot determine if account is credit or debit");
		}
	}

	public List<TransactionDTO> showItems(final Integer account, final Integer year, final Integer month) {
		log.info("showItems: {}, {}, {}", account, year, month);
		LocalDate startDate;
		LocalDate endDate;
		if(month == 0) { // get everything for the year
			startDate = LocalDate.of(year, 1, 1);
			endDate = startDate.plusYears(1).minusDays(1);
		} else {
			startDate = LocalDate.of(year, month, 1);
			endDate = startDate.plusMonths(1).minusDays(1);
		}

		try (
			Connection conn = dataSource.getConnection();
			PreparedStatement stmt = conn.prepareStatement(SUM_UP_TO_DATE)
		) {
			stmt.setInt(1, account);
			stmt.setDate(2, java.sql.Date.valueOf(startDate));
			stmt.execute();
			if(stmt.getResultSet().next()) {
				return getEntries(stmt.getResultSet().getBigDecimal("balance"), account, startDate, endDate);
			}
		} catch(SQLException e) {
			log.error(e.getMessage());
		}
		return new ArrayList<>();
	}

	public List<TransactionDTO> getEntries(final BigDecimal tempBal, final Integer account, final LocalDate startDate, final LocalDate endDate) {
		log.info("getEntires: tempBal = {}, account = {}, dates: {} - {}", tempBal, account, startDate, endDate);

		BigDecimal runningTotal = (tempBal == null ? new BigDecimal(0) : tempBal);
		List<TransactionDTO> entries = new ArrayList<>();

		try (
			Connection conn = dataSource.getConnection();
			PreparedStatement stmt = conn.prepareStatement(ONE_ACCOUNT_WITHIN_DATE)
		) {
			stmt.setInt(1, account);
			stmt.setDate(2, java.sql.Date.valueOf(startDate));
			stmt.setDate(3, java.sql.Date.valueOf(endDate));
			stmt.execute();
			while(stmt.getResultSet().next()) {
				ResultSet rs = stmt.getResultSet();
				BigDecimal amount = rs.getBigDecimal("amount");
				Boolean visible = rs.getBoolean("visible");
				if(visible) {
					runningTotal = runningTotal.add(amount);
				}

				entries.add(new TransactionDTO(
					rs.getInt("sequence"),
					amount,
					rs.getDate("mydate").toLocalDate(),
					rs.getString("reference"),
					rs.getBoolean("reconciled"),
					visible,
					rs.getString("entity"),
					rs.getString("method"),
					visible ? nf.format(runningTotal) : ""
				));
			}
		} catch(SQLException e) {
			log.error(e.getMessage());
		}
		return entries;
	}

	public BigDecimal getBalance(final Integer account) {
		try (
			Connection conn = dataSource.getConnection();
			PreparedStatement stmt = conn.prepareStatement(SUM_FOR_ACCOUNT)
		) {
			stmt.setInt(1, account);
			stmt.execute();
			if(stmt.getResultSet().next()) {
				return stmt.getResultSet().getBigDecimal("balance");
			}
		} catch(SQLException e) {
			log.error(e.getMessage());
		}
		return null;
	}

	public void updateIncludes(final IncludesModifyRequest req) {
		log.info("updateIncludes");

		actionRepository.setVisibleTrue(req.getAdd());
		actionRepository.removeVisibleTrue(req.getRemove());
	}

	public void reconcile(final ReconcileRequest req) {
		log.info("reconcile");

		if (!req.getEntries().isEmpty()) {
			actionRepository.reconcile(req.getEntries());
		}
	}
}
