package com.lavacro.finances.api.v1;

import com.lavacro.finances.domain.TransactionList;
import com.lavacro.finances.entities.ActionEntity;
import com.lavacro.finances.model.*;

import com.lavacro.finances.services.TransactionService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping(value = "/api/v1/transaction")
@Slf4j
public class TransactionAPI {
	private static final String SUCCESS = "success";

	private final TransactionService transactionService;

	public TransactionAPI(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@PostMapping
	public ActionResponse addTransaction(@Valid @RequestBody NewTransaction newTransaction) {
		transactionService.newTransaction(newTransaction);
		ActionResponse resp = new ActionResponse();
		resp.setCode(0);
		resp.setMessage(SUCCESS);
		return resp;
	}

	@PutMapping(value = "/include")
	public ActionResponse updateIncludes(@RequestBody final IncludesModifyRequest req) {
		ActionResponse resp = new ActionResponse();
		transactionService.updateIncludes(req);
		resp.setCode(0);
		resp.setMessage(SUCCESS);
		return resp;
	}

	@PutMapping(value = "/reconcile")
	public ActionResponse reconcile(@RequestBody final ReconcileRequest req) {
		ActionResponse resp = new ActionResponse();
		transactionService.reconcile(req);
		resp.setCode(0);
		resp.setMessage(SUCCESS);
		return resp;
	}

	@GetMapping(value = "/{sequence}")
	public ActionEntity getOneTransaction(@PathVariable("sequence") final Integer sequence) {
		log.info("getOneTransaction: {}", sequence);
		return transactionService.findOne(sequence);
	}

	@PutMapping
	public ActionResponse updateTransaction(@RequestBody final ActionEntity req) {
		transactionService.updateTransaction(req);
		ActionResponse resp = new ActionResponse();
		resp.setCode(0);
		resp.setMessage(SUCCESS);
		return resp;
	}

	@DeleteMapping(value = "/{sequence}")
	public ActionResponse deleteTransaction(@PathVariable("sequence") final Integer sequence) {
		transactionService.deleteTransaction(sequence);
		ActionResponse resp = new ActionResponse();
		resp.setCode(0);
		resp.setMessage(SUCCESS);
		return resp;
	}

	@GetMapping(value = "/list")
	public TransactionList listTransactions(
			@RequestParam("account") Integer account,
			@RequestParam("beginDate") String beginDate,
			@RequestParam("endDate") String endDate
	) {
		log.info("List transactions for account {} from {} to {}", account, beginDate, endDate);

		TransactionList transactionList = new TransactionList();
		try {
			LocalDate firstDate = LocalDate.parse(beginDate);
			LocalDate lastDate = LocalDate.parse(endDate);

			log.info("First date: {}", firstDate);
			log.info("Last date: {}", lastDate);
			transactionList.setTransactions(transactionService.getEntries(new BigDecimal(0), account, firstDate, lastDate));
			transactionList.setCode(0);
			transactionList.setMessage(SUCCESS);
		} catch(Exception e) {
			transactionList.setCode(1);
			transactionList.setMessage(e.getMessage());
			log.error("listTransactions: {}", e.getMessage());
		}
		return transactionList;
	}
}
