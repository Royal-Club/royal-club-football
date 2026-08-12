package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Sends one personalised email per player and reports who actually got one.
 * <p>
 * Shared by the RSVP, dues and password-reset flows because all three need the same three
 * guarantees: a rejected recipient must not abort the run, a failed send must never be recorded as
 * sent (or the player is skipped by tomorrow's de-duplication for mail they never received), and
 * members without a usable address are skipped quietly.
 * <p>
 * Each message is retried a few times with backoff, because most SMTP failures are transient and
 * the alternative is making a member wait a day for the next scheduled run. Every attempt is
 * counted into {@code club.mail.messages}, so a delivery rate that quietly falls to zero - a wrong
 * API key, an exhausted daily allowance - is visible in metrics rather than only in a member's
 * complaint.
 */
@Component
@Slf4j
public class ClubMailSender {

    private static final String METRIC_MESSAGES = "club.mail.messages";
    private static final String TAG_OUTCOME = "outcome";

    private final JavaMailSender mailSender;
    private final MeterRegistry meterRegistry;

    @Value("${mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${mail.from-address}")
    private String fromAddress;

    @Value("${mail.from-name}")
    private String fromName;

    @Value("${mail.max-attempts:3}")
    private int maxAttempts;

    @Value("${mail.retry-backoff-ms:1000}")
    private long retryBackoffMillis;

    public ClubMailSender(JavaMailSender mailSender, MeterRegistry meterRegistry) {
        this.mailSender = mailSender;
        this.meterRegistry = meterRegistry;
    }

    public String getFromName() {
        return fromName;
    }

    /**
     * @param contentBuilder produces the subject and both bodies for one player.
     * @return the players the mail server accepted.
     */
    public List<Player> sendEach(List<Player> players, String description,
                                 Function<Player, MailContent> contentBuilder) {
        if (!mailEnabled) {
            log.warn("Email delivery is disabled globally; skipping {}.", description);
            return List.of();
        }
        if (players == null || players.isEmpty()) {
            return List.of();
        }

        List<Player> delivered = new ArrayList<>();
        for (Player player : players) {
            // Player.email is NOT NULL in the schema, so this only screens out blank/placeholder values.
            if (player.getEmail() == null || player.getEmail().isBlank()) {
                log.debug("Player {} has no usable email address; skipping.", player.getId());
                count("skipped");
                continue;
            }
            if (sendWithRetry(player, description, contentBuilder)) {
                delivered.add(player);
            }
        }

        log.info("{}: emailed {} of {} player(s).", description, delivered.size(), players.size());
        return delivered;
    }

    /**
     * @return true only once the mail server has accepted the message.
     */
    private boolean sendWithRetry(Player player, String description,
                                  Function<Player, MailContent> contentBuilder) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                mailSender.send(build(player, contentBuilder.apply(player)));
                count("sent");
                return true;
            } catch (MailAuthenticationException e) {
                // Bad credentials will fail identically on every retry, and retrying only multiplies
                // how long the caller waits to discover a misconfiguration.
                log.error("Mail authentication failed sending {}; check the mail credentials.",
                        description, e);
                count("auth_failed");
                return false;
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    log.error("Failed to send {} to player {} after {} attempt(s).",
                            description, player.getId(), attempt, e);
                    count("failed");
                    return false;
                }
                long backoff = retryBackoffMillis * attempt;
                log.warn("Attempt {}/{} to send {} to player {} failed; retrying in {}ms.",
                        attempt, maxAttempts, description, player.getId(), backoff, e);
                if (!pause(backoff)) {
                    // Interrupted mid-backoff, most likely a shutdown. Abandon rather than hold it up.
                    count("failed");
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * @return false when the wait was interrupted, so the caller stops rather than retrying through
     * a shutdown.
     */
    private boolean pause(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void count(String outcome) {
        meterRegistry.counter(METRIC_MESSAGES, TAG_OUTCOME, outcome).increment();
    }

    private MimeMessage build(Player player, MailContent content) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
        helper.setFrom(fromAddress, fromName);
        helper.setTo(player.getEmail());
        helper.setSubject(content.subject());
        helper.setText(content.text(), content.html());
        return message;
    }

    /** Subject plus both bodies; the plain-text part is the fallback for clients that refuse HTML. */
    public record MailContent(String subject, String text, String html) {
    }
}
