package com.lavacro.finances.controllers;

import com.lavacro.finances.entities.EntityEntity;

import com.lavacro.finances.services.EntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.text.Normalizer;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class Entities {
	private final EntityService entityService;

	@GetMapping(value = "/entities")
	public String getEntities(Model model) {
		List<EntityEntity> entities = entityService.getAllEntities();
		model.addAttribute("entityList", entities);
		return "entities";
	}

	@PostMapping(value = "/entities", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public String addEntity(
		Model model,
		@RequestParam(value = "description") String description,
		@RequestParam(value = "account", required = false) String account,
		@RequestParam(value = "address", required = false) String address
	) {
		// Sanitize description to reduce XSS risk while allowing non-English characters.
		String sanitized = description == null ? null : description.trim();
		if (sanitized != null && !sanitized.isEmpty()) {
			// Normalize to a canonical form so visually-equivalent characters are consistent
			sanitized = Normalizer.normalize(sanitized, Normalizer.Form.NFKC);
			// Remove control characters (except common whitespace), keeping format
			// characters such as ZWNJ/ZWJ that are meaningful in some scripts
			sanitized = sanitized.replaceAll("[\\p{Cc}&&[^\\t\\n\\r]]|\\p{Cn}", "");
		}

		try {
			entityService.createEntity(sanitized, account, address);
		} catch(Exception e) {
			log.error("Error creating entity: {}", e.getMessage());
		}
		return getEntities(model);
	}
}
