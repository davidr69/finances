package com.lavacro.finances.domain.action;

import com.lavacro.finances.dto.TransactionDTO;
import com.lavacro.finances.dto.ActionDTO;
import com.lavacro.finances.dto.TransactionTypeDTO;
import com.lavacro.finances.model.*;

import com.lavacro.finances.repositories.jdbc.TransactionTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActionService {
	private final TransactionTypeRepository transactionTypeRepository;
	private final ActionRepository actionRepository;
	private final JdbcClient jdbcClient;

	private static final NumberFormat nf = NumberFormat.getInstance();

	static {
		nf.setMinimumFractionDigits(2);
	}

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

	@Language("SQL")
	private static final String SET_VISIBLE_TRUE = "UPDATE action SET visible = 't' WHERE sequence IN (:ids)";

	@Language("SQL")
	private static final String REMOVE_VISIBLE_TRUE = "UPDATE action SET visible = 'f' WHERE sequence IN (:ids)";

	@Language("SQL")
	private static final String RECONCILE = "UPDATE action SET reconciled = 't', visible = 't' WHERE sequence IN (:ids)";

	List<TransactionTypeDTO> findAllOrderByDescriptionAsc() {
		return transactionTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "description"));
	}

	ActionResponse persistTransaction(NewTransaction req, String plusOrMinus) {
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

		ActionDTO actionDTO = new ActionDTO();
		actionDTO.setEntity(req.getEntity());
		actionDTO.setAccount(req.getAccount());
		actionDTO.setAmount(amount);
		actionDTO.setMydate(LocalDate.of(req.getYear(), req.getMonth(), req.getDay()));
		actionDTO.setMethod(req.getMethod());
		if(!req.getReference().isEmpty()) {
			actionDTO.setReference(req.getReference());
		}
		if(req.getCategory() != null) {
			actionDTO.setCategory(req.getCategory());
		}
		actionRepository.save(actionDTO);
		resp.setCode(0);
		resp.setMessage("Successfully added");
		return resp;
	}

	void deleteTransaction(final Integer id) {
		actionRepository.deleteById(id);
	}

	ActionDTO findOne(final Integer id) {
		return actionRepository.findById(id).orElse(null);
	}

	void updateTransaction(final ActionDTO actionDTO) {
		if(actionDTO.getReference().isBlank()) {
			actionDTO.setReference(null);
		}
		actionRepository.save(actionDTO);
	}

	void newTransaction(final NewTransaction newTransaction) {
		TransactionTypeDTO ttype =  transactionTypeRepository.findById(newTransaction.getMethod()).orElse(null);
		if(ttype != null) {
			ActionResponse resp = persistTransaction(newTransaction, ttype.creditDebit());
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
			startDate = LocalDate.of(year, Month.JANUARY, 1);
			endDate = startDate.plusYears(1).minusDays(1);
		} else {
			startDate = LocalDate.of(year, month, 1);
			endDate = startDate.plusMonths(1).minusDays(1);
		}

		Optional<BigDecimal> bal = jdbcClient.sql(SUM_UP_TO_DATE).params(account,startDate).query(BigDecimal.class).optional();
		BigDecimal balance = bal.orElse(BigDecimal.ZERO);

		return getEntries(balance, account, startDate, endDate);
	}

	List<TransactionDTO> getEntries(final BigDecimal tempBal, final Integer account, final LocalDate startDate, final LocalDate endDate) {
		log.info("getEntries: tempBal = {}, account = {}, dates: {} - {}", tempBal, account, startDate, endDate);

		// Using an array or an AtomicReference if you were in a true stream,
		// but in a simple RowMapper, a local variable works fine.
		final BigDecimal[] runningTotal = { (tempBal == null ? BigDecimal.ZERO : tempBal) };

		return jdbcClient.sql(ONE_ACCOUNT_WITHIN_DATE)
				.params(List.of(account, java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate)))
				.query((rs, rowNum) -> {
					BigDecimal amount = rs.getBigDecimal("amount");
					boolean visible = rs.getBoolean("visible");

					if (visible) {
						runningTotal[0] = runningTotal[0].add(amount);
					}

					return new TransactionDTO(
							rs.getInt("sequence"),
							amount,
							rs.getDate("mydate").toLocalDate(),
							rs.getString("reference"),
							rs.getBoolean("reconciled"),
							visible,
							rs.getString("entity"),
							rs.getString("method"),
							visible ? nf.format(runningTotal[0]) : ""
					);
				})
				.list();
	}

	public BigDecimal getBalance(final Integer account) {
		Optional<BigDecimal> bal = jdbcClient.sql(SUM_FOR_ACCOUNT).params(account).query(BigDecimal.class).optional();
		return bal.orElse(BigDecimal.ZERO);
	}

	void updateIncludes(final IncludesModifyRequest req) {
		log.info("updateIncludes");

		if(req.getAdd() != null && !req.getAdd().isEmpty()) {
			log.info("Adding {}", req.getAdd());
			jdbcClient.sql(SET_VISIBLE_TRUE).param("ids", req.getAdd()).update();
		}
		if(req.getRemove() != null && !req.getRemove().isEmpty()) {
			log.info("Removing {}", req.getRemove());
			jdbcClient.sql(REMOVE_VISIBLE_TRUE).param("ids", req.getRemove()).update();
		}
	}

	void reconcile(final ReconcileRequest req) {
		log.info("reconcile");

		if (!req.getEntries().isEmpty()) {
			jdbcClient.sql(RECONCILE).param("ids", req.getEntries()).update();
		}
	}
}
