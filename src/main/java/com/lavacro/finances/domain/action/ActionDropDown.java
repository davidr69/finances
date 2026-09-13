package com.lavacro.finances.domain.action;

import com.lavacro.finances.dto.TransactionTypeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/select")
@RequiredArgsConstructor
public class ActionDropDown {
	private final ActionService actionService;

	@GetMapping(value = "/transaction_types")
	public List<TransactionTypeDTO> getTransactionTypes() {
		return actionService.findAllOrderByDescriptionAsc();
	}

}
