package io.softwarestrategies.tradescout.controller;

import io.softwarestrategies.tradescout.dto.OpportunitySignal;
import io.softwarestrategies.tradescout.service.OpportunityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for opportunity-related endpoints
 */
@RestController
@RequestMapping("/opportunities")
public class OpportunityController {

	private static final Logger log = LoggerFactory.getLogger(OpportunityController.class);

	private final OpportunityService opportunityService;

	public OpportunityController(OpportunityService opportunityService) {
		this.opportunityService = opportunityService;
	}

	/**
	 * Get current opportunities
	 */
	@GetMapping
	public ResponseEntity<List<OpportunitySignal>> getCurrentOpportunities() {
		log.info("GET /opportunities - Fetching current opportunities");
		var opportunities = opportunityService.scanAndAlert();
		return ResponseEntity.ok(opportunities);
	}

	/**
	 * Manually trigger a scan (for testing)
	 */
	@PostMapping("/scan")
	public ResponseEntity<Map<String, Object>> triggerScan() {
		log.info("POST /opportunities/scan - Manual scan triggered");

		var opportunities = opportunityService.scanAndAlert();

		return ResponseEntity.ok(Map.of(
				"message", "Scan complete",
				"opportunitiesFound", opportunities.size(),
				"opportunities", opportunities
		));
	}
}