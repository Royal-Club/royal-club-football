package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.util.RsvpTokenUtil;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RsvpEmailServiceImpl implements RsvpEmailService {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy 'at' hh:mm a");

    private final JavaMailSender mailSender;
    private final RsvpTokenUtil rsvpTokenUtil;

    @Value("${mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${mail.from-address}")
    private String fromAddress;

    @Value("${mail.from-name}")
    private String fromName;

    @Value("${rsvp.landing-url}")
    private String landingUrl;

    @Value("${reminders.zone:Asia/Dhaka}")
    private String displayZone;

    public RsvpEmailServiceImpl(JavaMailSender mailSender, RsvpTokenUtil rsvpTokenUtil) {
        this.mailSender = mailSender;
        this.rsvpTokenUtil = rsvpTokenUtil;
    }

    @Override
    public List<Player> sendRsvpEmails(Tournament tournament, List<Player> players, boolean invitation) {
        if (!mailEnabled) {
            log.warn("Email delivery is disabled globally; skipping RSVP email for tournament {}.",
                    tournament.getId());
            return List.of();
        }
        if (players == null || players.isEmpty()) {
            return List.of();
        }

        String subject = invitation
                ? String.format("⚽ %s - are you playing?", tournament.getName())
                : String.format("⏰ Reminder: %s needs your Yes or No", tournament.getName());

        List<Player> delivered = new ArrayList<>();
        for (Player player : players) {
            // Player.email is NOT NULL in the schema, so this only screens out blank/placeholder values.
            if (player.getEmail() == null || player.getEmail().isBlank()) {
                log.debug("Player {} has no usable email address; skipping.", player.getId());
                continue;
            }
            try {
                mailSender.send(buildMessage(tournament, player, subject, invitation));
                delivered.add(player);
            } catch (Exception e) {
                // One rejected recipient must not stop the rest of the run, and must not be logged as sent.
                log.error("Failed to email RSVP {} for tournament {} to player {}.",
                        invitation ? "invitation" : "reminder", tournament.getId(), player.getId(), e);
            }
        }

        log.info("Tournament '{}' ({}): emailed {} of {} player(s).",
                tournament.getName(), tournament.getId(), delivered.size(), players.size());
        return delivered;
    }

    private MimeMessage buildMessage(Tournament tournament, Player player, String subject, boolean invitation)
            throws jakarta.mail.MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
        helper.setFrom(fromAddress, fromName);
        helper.setTo(player.getEmail());
        helper.setSubject(subject);

        String yesUrl = buildVoteUrl(tournament, player, true);
        String noUrl = buildVoteUrl(tournament, player, false);
        helper.setText(buildTextBody(tournament, player, invitation, yesUrl, noUrl),
                buildHtmlBody(tournament, player, invitation, yesUrl, noUrl));
        return message;
    }

    /**
     * Links point at the public confirmation page rather than an endpoint that votes on GET:
     * corporate mail scanners follow links in transit, and a scanner must not be able to cast a vote.
     */
    private String buildVoteUrl(Tournament tournament, Player player, boolean attending) {
        String token = rsvpTokenUtil.generate(tournament.getId(), player.getId(), attending,
                tournament.getTournamentDate());
        return UriComponentsBuilder.fromUriString(landingUrl)
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    /**
     * Tournament dates are stored in UTC, so they must be rendered back in club time - otherwise a
     * 4:00 PM kickoff reaches members as 10:00 AM.
     */
    private String formatKickoff(Tournament tournament) {
        return tournament.getTournamentDate()
                .atOffset(ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of(displayZone))
                .format(DISPLAY_FORMAT);
    }

    private String buildTextBody(Tournament tournament, Player player, boolean invitation,
                                 String yesUrl, String noUrl) {
        String opening = invitation
                ? "A new tournament has been scheduled."
                : "You have not confirmed yet, and kickoff is close.";
        return String.format("""
                        Hi %s,

                        %s

                        %s
                        When:  %s
                        Where: %s

                        Are you playing?

                        YES - %s
                        NO  - %s

                        You will be asked to confirm on the page before your answer is saved.
                        You can change your answer any time before kickoff.

                        - %s
                        """,
                player.getName(), opening, tournament.getName(),
                formatKickoff(tournament),
                tournament.getVenue() != null ? tournament.getVenue().getName() : "To be announced",
                yesUrl, noUrl, fromName);
    }

    private String buildHtmlBody(Tournament tournament, Player player, boolean invitation,
                                 String yesUrl, String noUrl) {
        String opening = invitation
                ? "A new tournament has been scheduled."
                : "You have not confirmed yet, and kickoff is close.";
        String venue = tournament.getVenue() != null ? tournament.getVenue().getName() : "To be announced";

        // Inline styles and a table shell: email clients strip <style> blocks and ignore flex/grid.
        return String.format("""
                        <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6f8;padding:24px 0;">
                          <tr><td align="center">
                            <table width="100%%" cellpadding="0" cellspacing="0" style="max-width:520px;background:#ffffff;border-radius:12px;padding:32px;font-family:Arial,Helvetica,sans-serif;color:#1f2933;">
                              <tr><td style="font-size:16px;">Hi %s,</td></tr>
                              <tr><td style="padding-top:12px;font-size:15px;color:#52606d;">%s</td></tr>
                              <tr><td style="padding-top:20px;font-size:20px;font-weight:bold;">%s</td></tr>
                              <tr><td style="padding-top:12px;font-size:15px;color:#52606d;">
                                <strong>When:</strong> %s<br/>
                                <strong>Where:</strong> %s
                              </td></tr>
                              <tr><td style="padding-top:28px;font-size:16px;font-weight:bold;">Are you playing?</td></tr>
                              <tr><td style="padding-top:16px;">
                                <a href="%s" style="display:inline-block;background:#0b8043;color:#ffffff;text-decoration:none;padding:12px 32px;border-radius:6px;font-size:15px;font-weight:bold;margin-right:12px;">YES</a>
                                <a href="%s" style="display:inline-block;background:#c5221f;color:#ffffff;text-decoration:none;padding:12px 32px;border-radius:6px;font-size:15px;font-weight:bold;">NO</a>
                              </td></tr>
                              <tr><td style="padding-top:24px;font-size:13px;color:#7b8794;">
                                You will be asked to confirm on the page before your answer is saved,
                                and you can change it any time before kickoff.
                              </td></tr>
                              <tr><td style="padding-top:20px;font-size:13px;color:#9aa5b1;">- %s</td></tr>
                            </table>
                          </td></tr>
                        </table>
                        """,
                player.getName(), opening, tournament.getName(),
                formatKickoff(tournament), venue,
                yesUrl, noUrl, fromName);
    }
}
