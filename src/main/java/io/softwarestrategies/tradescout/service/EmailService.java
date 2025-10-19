package io.softwarestrategies.tradescout.service;

import io.softwarestrategies.tradescout.config.TradeScoutProperties;
import io.softwarestrategies.tradescout.dto.OpportunitySignal;
import io.softwarestrategies.tradescout.dto.QuarterlyReport;
import io.softwarestrategies.tradescout.dto.TradeSetup;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service for sending email notifications
 */
@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;
	private final TradeScoutProperties properties;

	public EmailService(
			JavaMailSender mailSender,
			TemplateEngine templateEngine,
			TradeScoutProperties properties) {
		this.mailSender = mailSender;
		this.templateEngine = templateEngine;
		this.properties = properties;
	}

	/**
	 * Send opportunity alert email
	 */
	public void sendOpportunityAlert(OpportunitySignal signal, TradeSetup setup) {
		if (!properties.getTrading().getEmail().getEnabled()) {
			log.debug("Email alerts disabled");
			return;
		}

		try {
			var context = new Context();
			context.setVariable("signal", signal);
			context.setVariable("setup", setup);
			context.setVariable("includeLinks", properties.getTrading().getEmail().getIncludeLinks());

			var htmlContent = templateEngine.process("opportunity-email", context);

			var subject = String.format("🎯 OPPORTUNITY: %s (%.0f%% confidence)", signal.symbol(), signal.confidence());

			sendHtmlEmail(
					properties.getTrading().getEmail().getTo(),
					subject,
					htmlContent
			);

			log.info("Opportunity alert sent for {}", signal.symbol());

		} catch (Exception e) {
			log.error("Failed to send opportunity alert: {}", e.getMessage());
		}
	}

	/**
	 * Send quarterly report email
	 */
	public void sendQuarterlyReport(QuarterlyReport report) {
		if (!properties.getTrading().getEmail().getEnabled()) {
			return;
		}

		try {
			var context = new Context();
			context.setVariable("report", report);

			var htmlContent = templateEngine.process("quarterly-report", context);

			var subject = String.format("📊 TradeScout Quarterly Report - %s to %s", report.metrics().getPeriodStart(), report.metrics().getPeriodEnd());

			sendHtmlEmail(
					properties.getTrading().getEmail().getTo(),
					subject,
					htmlContent
			);

			log.info("Quarterly report sent");

		} catch (Exception e) {
			log.error("Failed to send quarterly report: {}", e.getMessage());
		}
	}

	/**
	 * Send plain text email (for testing)
	 */
	public void sendPlainEmail(String to, String subject, String text) throws MessagingException {
		var message = mailSender.createMimeMessage();
		var helper = new MimeMessageHelper(message, true, "UTF-8");

		helper.setFrom(properties.getTrading().getEmail().getFrom());
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(text, false);

		mailSender.send(message);
	}

	/**
	 * Send HTML email
	 */
	private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
		var message = mailSender.createMimeMessage();
		var helper = new MimeMessageHelper(message, true, "UTF-8");

		helper.setFrom(properties.getTrading().getEmail().getFrom());
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(htmlContent, true);

		mailSender.send(message);
	}
}