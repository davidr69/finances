package com.lavacro.finances.controllers;

import com.lavacro.finances.entities.EntityEntity;
import com.lavacro.finances.repositories.MerchantRepository;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Entities {
	private final MerchantRepository merchantRepository;

	public Entities(MerchantRepository merchantRepository) {
		this.merchantRepository = merchantRepository;
	}

	@GetMapping(value = "/entities")
	public String getEntities(Model model) {
		model.addAttribute("entityList", merchantRepository.findAllOrderByDescriptionAsc());
		return "entities";
	}

	@PostMapping(value = "/entities", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public String addEntity(
		Model model,
		@RequestParam(value = "description") String description,
		@RequestParam(value = "account", required = false) String account,
		@RequestParam(value = "address", required = false) String address
	) {
		EntityEntity merchant = new EntityEntity();
		merchant.setDescription(description);
		merchant.setAccount(account);
		merchant.setAddress(address);
		merchantRepository.save(merchant);
		model.addAttribute("entityList", merchantRepository.findAllOrderByDescriptionAsc());
		return "entities";
	}
}
