package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
 * Shared by the RSVP and dues reminders because both need the same three guarantees: a rejected
 * recipient must not abort the run, a failed send must never be recorded as sent (or the player is
 * skipped by tomorrow's de-duplication for mail they never received), and members without a usable
 * address are skipped quietly.
 */
@Component
@Slf4j
public class ClubMailSender {

    private final JavaMailSender mailSender;

    @Value("${mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${mail.from-address}")
    private String fromAddress;

    @Value("${mail.from-name}")
    private String fromName;

    public ClubMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
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
                continue;
            }
            try {
                MailContent content = contentBuilder.apply(player);
                mailSender.send(build(player, content));
                delivered.add(player);
            } catch (Exception e) {
                log.error("Failed to send {} to player {}.", description, player.getId(), e);
            }
        }

        log.info("{}: emailed {} of {} player(s).", description, delivered.size(), players.size());
        return delivered;
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
