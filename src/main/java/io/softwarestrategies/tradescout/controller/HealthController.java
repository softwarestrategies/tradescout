package io.softwarestrategies.tradescout.controller;

import io.softwarestrategies.tradescout.repository.StockHistoryRepository;
import io.softwarestrategies.tradescout.service.RiskManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

/**
 * Health check and status endpoint
 */
@RestController
@RequestMapping("/health")
public class HealthController {

	private final StockHistoryRepository stockHistoryRepository;
	private final RiskManagementService riskManagementService;

	public HealthController(
			StockHistoryRepository stockHistoryRepository,
			RiskManagementService riskManagementService) {
		this.stockHistoryRepository = stockHistoryRepository;
		this.riskManagementService = riskManagementService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> healthCheck() {
		var symbols = stockHistoryRepository.findAllDistinctSymbols();
		var canTrade = riskManagementService.canTakeNewTrade();

		return ResponseEntity.ok(Map.of(
				"status", "UP",
				"service", "TradeScout",
				"date", LocalDate.now(),
				"stocksTracked", symbols.size(),
				"canTrade", canTrade,
				"riskStatus", riskManagementService.getRiskStatus()
		));
	}
}