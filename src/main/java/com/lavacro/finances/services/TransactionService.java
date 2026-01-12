package com.lavacro.finances.services;

import com.lavacro.finances.repositories.TransactionRepository;
import com.lavacro.finances.entities.SumEntity;
import com.lavacro.finances.entities.TransactionEntity;
import com.lavacro.finances.entities.ActionEntity;
import com.lavacro.finances.entities.TransactionTypeEntity;
import com.lavacro.finances.model.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class TransactionService {
	private final TransactionTypeRepository transactionTypeRepository;
	private final ActionRepository actionRepository;
	private final SumRepository sumRepository;
	private final TransactionRepository transactionRepository;

	TransactionService(
			TransactionTypeRepository transactionTypeRepository,
			ActionRepository actionRepository,
			SumRepository sumRepository,
			TransactionRepository transactionRepository
	) {
		this.transactionTypeRepository = transactionTypeRepository;
		this.actionRepository = actionRepository;
		this.sumRepository = sumRepository;
		this.transactionRepository = transactionRepository;
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

	public List<TransactionEntity> showItems(final Integer account, final Integer year, final Integer month) {
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

		SumEntity sum = sumRepository.getSumUpToDate(account, startDate);
		List<TransactionEntity> resp = null;

		if(sum != null) {
			BigDecimal tempBal = sum.getBalance();
			resp = getEntries(tempBal, account, startDate, endDate);
		}
		return resp;
	}

	public List<TransactionEntity> getEntries(final BigDecimal tempBal, final Integer account, final LocalDate startDate, final LocalDate endDate) {
		log.info("getEntires: tempBal = {}, account = {}, dates: {} - {}", tempBal, account, startDate, endDate);
		List<TransactionEntity> resp = transactionRepository.findForOneAccount(account, startDate, endDate);

		final NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);

		BigDecimal runningTotal = (tempBal == null ? new BigDecimal(0) : tempBal);
		for(TransactionEntity transactionEntity : resp) {
			if(transactionEntity.isVisible()) {
				runningTotal = runningTotal.add(transactionEntity.getAmount());
				transactionEntity.setRunningTotal(nf.format(runningTotal));
			}
		}
		return resp;
	}

	public BigDecimal getBalance(final Integer account) {
		SumEntity sum = sumRepository.getSumForAccount(account);
		if(sum != null) {
			return sum.getBalance();
		} else {
			return null;
		}
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

@Repository
interface TransactionTypeRepository extends JpaRepository<TransactionTypeEntity, Integer> {
}

@Repository
interface SumRepository extends JpaRepository<SumEntity, Integer> {
	@Query(value = """
		SELECT 1 AS rownum, SUM(amount) AS balance
		FROM action
		WHERE account = :account AND visible = 't' AND mydate < :mydate
	""", nativeQuery = true)
	SumEntity getSumUpToDate(
			@Param("account") final Integer account,
			@Param("mydate") final LocalDate mydate
	);

	@Query(value = """
		SELECT 2 AS rownum, SUM(amount) AS balance
		FROM action
		WHERE account = :account AND reconciled='t'
	""", nativeQuery = true)
	SumEntity getSumForAccount(
			@Param("account") final Integer account
	);
}

@Repository
interface ActionRepository extends JpaRepository<ActionEntity, Integer> {
	@Modifying
	@Transactional
	@Query(value = "UPDATE action SET visible = 't' WHERE sequence IN :visible_list", nativeQuery = true)
	void setVisibleTrue(@Param("visible_list") final List<Integer> visibleList);

	@Modifying
	@Transactional
	@Query(value = "UPDATE action SET visible = 'f' WHERE sequence IN :visible_list", nativeQuery = true)
	void removeVisibleTrue(@Param("visible_list") final List<Integer> visibleList);

	@Modifying
	@Transactional
	@Query(value = "UPDATE action SET reconciled = 't', visible = 't' WHERE sequence IN :reconcile_list", nativeQuery = true)
	void reconcile(@Param("reconcile_list") final List<Integer> reconcileList);
}
