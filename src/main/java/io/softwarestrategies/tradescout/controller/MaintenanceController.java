package io.softwarestrategies.tradescout.controller;

import io.softwarestrategies.tradescout.service.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for maintenance operations
 */
@RestController
@RequestMapping("/maintenance")
public class MaintenanceController {

	private static final Logger log = LoggerFactory.getLogger(MaintenanceController.class);

	private final MarketDataService marketDataService;

	public MaintenanceController(MarketDataService marketDataService) {
		this.marketDataService = marketDataService;
	}

	/**
	 * Initialize historical data (first time setup)
	 */
	@PostMapping("/initialize")
	public ResponseEntity<Map<String, String>> initializeData() {
		log.info("POST /maintenance/initialize - Starting initial data load");

		try {
			marketDataService.loadInitialData();
			return ResponseEntity.ok(Map.of(
					"status", "success",
					"message", "Historical data loaded successfully"
			));
		} catch (Exception e) {
			log.error("Failed to initialize data", e);
			return ResponseEntity.internalServerError().body(Map.of(
					"status", "error",
					"message", e.getMessage()
			));
		}
	}

	/**
	 * Update today's market data
	 */
	@PostMapping("/update")
	public ResponseEntity<Map<String, String>> updateData() {
		log.info("POST /maintenance/update - Updating today's data");

		try {
			marketDataService.updateTodaysData();
			marketDataService.calculateMetricsForAll();

			return ResponseEntity.ok(Map.of(
					"status", "success",
					"message", "Data updated successfully"
			));
		} catch (Exception e) {
			log.error("Failed to update data", e);
			return ResponseEntity.internalServerError().body(Map.of(
					"status", "error",
					"message", e.getMessage()
			));
		}
	}
}