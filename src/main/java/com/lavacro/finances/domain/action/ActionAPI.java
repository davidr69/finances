package com.lavacro.finances.domain.action;

import com.lavacro.finances.domain.TransactionList;
import com.lavacro.finances.dto.ActionDTO;
import com.lavacro.finances.model.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping(value = "/api/v1/transaction")
@Slf4j
@RequiredArgsConstructor
public class ActionAPI {
	private static final String SUCCESS = "success";

	private final ActionService actionService;

	@PostMapping
	public ActionResponse addTransaction(@Valid @RequestBody NewTransaction newTransaction) {
		actionService.newTransaction(newTransaction);
		ActionResponse resp = new ActionResponse();
		resp.setCode(0);
		resp.setMessage(SUCCESS);
		return resp;
	}

	@PutMapping(value = "/include")
	public ActionResponse updateIncludes(@RequestBody final IncludesModifyRequest req) {
		ActionResponse resp = new ActionResponse();
		actionService.updateIncludes(req);
		resp.setCode(0);
		resp.setMessage(SUCCESS);
		return resp;
	}

	@PutMapping(value = "/reconcile")
	public ActionResponse reconcile(@RequestBody final ReconcileRequest req) {
		ActionResponse resp = new ActionResponse();
		actionService.reconcile(req);
		resp.setCode(0);
		resp.setMessage(SUCCESS);
		return resp;
	}

	@GetMapping(value = "/{sequence}")
	public ActionDTO getOneTransaction(@PathVariable("sequence") final Integer sequence) {
		log.info("getOneTransaction: {}", sequence);
		return actionService.findOne(sequence);
	}

	@PutMapping
	public ActionResponse updateTransaction(@RequestBody final ActionDTO req) {
		actionService.updateTransaction(req);
		ActionResponse resp = new ActionResponse();
		resp.setCode(0);
		resp.setMessage(SUCCESS);
		return resp;
	}

	@DeleteMapping(value = "/{sequence}")
	public ActionResponse deleteTransaction(@PathVariable("sequence") final Integer sequence) {
		actionService.deleteTransaction(sequence);
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
			transactionList.setTransactions(actionService.getEntries(new BigDecimal(0), account, firstDate, lastDate));
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
