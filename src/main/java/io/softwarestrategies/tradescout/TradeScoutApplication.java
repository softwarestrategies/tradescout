package io.softwarestrategies.tradescout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * TradeScout - Professional Trading Opportunity Scanner
 *
 * Main application entry point that initializes the Spring Boot application
 * with virtual threads enabled for efficient concurrent processing.
 *
 * Built with Java 25 featuring:
 * - Virtual Threads (Project Loom)
 * - Pattern Matching for switch
 * - Record Patterns
 * - String Templates (Preview)
 * - Sequenced Collections
 *
 * @author Software Strategies
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class TradeScoutApplication {

	private static final Logger log = LoggerFactory.getLogger(TradeScoutApplication.class);

	public static void main(String[] args) {
		logStartupBanner();

		// Enable virtual threads for platform threads
		System.setProperty("spring.threads.virtual.enabled", "true");

		SpringApplication.run(TradeScoutApplication.class, args);
	}

	private static void logStartupBanner() {
		var javaVersion = System.getProperty("java.version");
		var javaVendor = System.getProperty("java.vendor");
		var jvmName = System.getProperty("java.vm.name");
		var springBootVersion = SpringApplication.class.getPackage().getImplementationVersion();

		log.info("═══════════════════════════════════════════════════════════");
		log.info("  _____              _       _____                 _     ");
		log.info(" |_   _| __ __ _  __| | ___ / ____|               | |    ");
		log.info("   | || '__/ _` |/ _` |/ _ \\___ \\  ___ ___  _   _| |_   ");
		log.info("   | || | | (_| | (_| |  __/___) |/ __/ _ \\| | | | __|  ");
		log.info("   |_||_|  \\__,_|\\__,_|\\___|____/ \\___\\___/|_| |_|\\__|");
		log.info("                                                          ");
		log.info("      Professional Trading Opportunity Scanner           ");
		log.info("═══════════════════════════════════════════════════════════");
		log.info("Organization: Software Strategies");
		log.info("Java Version: {}", javaVersion);
		log.info("Java Vendor: {}", javaVendor);
		log.info("JVM: {}", jvmName);
		log.info("Spring Boot: {}", springBootVersion != null ? springBootVersion : "3.4.0");
		log.info("Virtual Threads: Enabled");
		log.info("═══════════════════════════════════════════════════════════");
	}
}