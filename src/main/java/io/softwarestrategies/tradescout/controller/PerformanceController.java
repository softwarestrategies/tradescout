package io.softwarestrategies.tradescout.controller;

import io.softwarestrategies.tradescout.dto.PerformanceSnapshot;
import io.softwarestrategies.tradescout.dto.QuarterlyReport;
import io.softwarestrategies.tradescout.service.PerformanceTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for performance metrics
 */
@RestController
@RequestMapping("/performance")
public class PerformanceController {

	private static final Logger log = LoggerFactory.getLogger(PerformanceController.class);

	private final PerformanceTrackingService performanceService;

	public PerformanceController(PerformanceTrackingService performanceService) {
		this.performanceService = performanceService;
	}

	/**
	 * Get current performance snapshot
	 */
	@GetMapping("/current")
	public ResponseEntity<PerformanceSnapshot> getCurrentPerformance() {
		log.info("GET /performance/current");
		var snapshot = performanceService.getCurrentPerformance();
		return ResponseEntity.ok(snapshot);
	}

	/**
	 * Generate quarterly report
	 */
	@GetMapping("/quarterly")
	public ResponseEntity<QuarterlyReport> getQuarterlyReport() {
		log.info("GET /performance/quarterly");
		var report = performanceService.generateQuarterlyReport();
		return ResponseEntity.ok(report);
	}
}