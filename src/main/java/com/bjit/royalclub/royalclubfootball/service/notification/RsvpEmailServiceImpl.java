package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.Venue;
import com.bjit.royalclub.royalclubfootball.util.RsvpTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RsvpEmailServiceImpl implements RsvpEmailService {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy 'at' hh:mm a");
    private static final String MAPS_SEARCH_URL = "https://www.google.com/maps/search/?api=1&query=%s";
    private static final String VENUE_UNKNOWN = "To be announced";

    private final ClubMailSender clubMailSender;
    private final RsvpTokenUtil rsvpTokenUtil;

    @Value("${rsvp.landing-url}")
    private String landingUrl;

    @Value("${reminders.zone:Asia/Dhaka}")
    private String displayZone;

    public RsvpEmailServiceImpl(ClubMailSender clubMailSender, RsvpTokenUtil rsvpTokenUtil) {
        this.clubMailSender = clubMailSender;
        this.rsvpTokenUtil = rsvpTokenUtil;
    }

    @Override
    public List<Player> sendRsvpEmails(Tournament tournament, List<Player> players, boolean invitation) {
        String subject = invitation
                ? String.format("⚽ %s - are you playing?", tournament.getName())
                : String.format("⏰ Reminder: %s needs your Yes or No", tournament.getName());

        String description = String.format("RSVP %s for tournament %d",
                invitation ? "invitation" : "reminder", tournament.getId());

        return clubMailSender.sendEach(players, description, player -> {
            // Signed per player, so each recipient's links only ever answer for themselves.
            String yesUrl = buildVoteUrl(tournament, player, true);
            String noUrl = buildVoteUrl(tournament, player, false);
            return new ClubMailSender.MailContent(subject,
                    buildTextBody(tournament, player, invitation, yesUrl, noUrl),
                    buildHtmlBody(tournament, player, invitation, yesUrl, noUrl));
        });
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
                        %s

                        Are you playing?

                        YES - %s
                        NO  - %s

                        You will be asked to confirm on the page before your answer is saved.
                        You can change your answer any time before kickoff.

                        - %s
                        """,
                player.getName(), opening, tournament.getName(),
                formatKickoff(tournament), buildTextVenue(tournament.getVenue()),
                yesUrl, noUrl, clubMailSender.getFromName());
    }

    private String buildHtmlBody(Tournament tournament, Player player, boolean invitation,
                                 String yesUrl, String noUrl) {
        String opening = invitation
                ? "A new tournament has been scheduled."
                : "You have not confirmed yet, and kickoff is close.";

        // Inline styles and a table shell: email clients strip <style> blocks and ignore flex/grid.
        return String.format("""
                        <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6f8;padding:24px 0;">
                          <tr><td align="center">
                            <table width="100%%" cellpadding="0" cellspacing="0" style="max-width:520px;background:#ffffff;border-radius:12px;padding:32px;font-family:Arial,Helvetica,sans-serif;color:#1f2933;">
                              <tr><td style="font-size:16px;">Hi %s,</td></tr>
                              <tr><td style="padding-top:12px;font-size:15px;color:#52606d;">%s</td></tr>
                              <tr><td style="padding-top:20px;font-size:20px;font-weight:bold;">%s</td></tr>
                              <tr><td style="padding-top:12px;font-size:15px;color:#52606d;">
                                <strong>When:</strong> %s
                              </td></tr>
                              <tr><td style="padding-top:6px;font-size:15px;color:#52606d;">%s</td></tr>
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
                formatKickoff(tournament), buildHtmlVenue(tournament.getVenue()),
                yesUrl, noUrl, clubMailSender.getFromName());
    }

    /**
     * The link a player actually taps, in order of preference: the venue's own Google Maps share link,
     * then the address when an admin has pasted a link into that field instead, and finally a Maps search
     * built from a street address. Null when there is nothing to point at.
     */
    private String buildMapUrl(Venue venue) {
        if (venue == null) {
            return null;
        }
        if (StringUtils.hasText(venue.getMapUrl())) {
            return venue.getMapUrl().trim();
        }
        if (isLink(venue.getAddress())) {
            return venue.getAddress().trim();
        }
        if (StringUtils.hasText(venue.getAddress())) {
            return String.format(MAPS_SEARCH_URL,
                    URLEncoder.encode(venue.getAddress().trim(), StandardCharsets.UTF_8));
        }
        return null;
    }

    /**
     * Address fields predate the dedicated map link, so some already hold a pasted Maps URL. Such an
     * address is only ever used as the link: searching Maps for the literal URL finds nothing, and
     * printing it where a street address belongs tells a player nothing about the ground.
     */
    private boolean isLink(String address) {
        if (!StringUtils.hasText(address)) {
            return false;
        }
        String trimmed = address.trim().toLowerCase();
        return trimmed.startsWith("http://") || trimmed.startsWith("https://");
    }

    private boolean hasReadableAddress(Venue venue) {
        return StringUtils.hasText(venue.getAddress()) && !isLink(venue.getAddress());
    }

    private String buildTextVenue(Venue venue) {
        if (venue == null) {
            return "Where: " + VENUE_UNKNOWN;
        }
        // Continuation lines are padded to the width of "Where: " so the block stays aligned under it.
        StringBuilder block = new StringBuilder("Where: ").append(venue.getName());
        if (hasReadableAddress(venue)) {
            block.append("\n       ").append(venue.getAddress().trim());
        }
        String mapUrl = buildMapUrl(venue);
        if (mapUrl != null) {
            block.append("\nMap:   ").append(mapUrl);
        }
        return block.toString();
    }

    /**
     * Venue name, then the address in a quieter tone, then an outlined "Open in Google Maps" chip -
     * outlined rather than filled so it stays clearly secondary to the YES/NO buttons below it.
     */
    private String buildHtmlVenue(Venue venue) {
        if (venue == null) {
            return "<strong>Where:</strong> " + VENUE_UNKNOWN;
        }
        StringBuilder block = new StringBuilder("<strong>Where:</strong> ")
                .append(escapeHtml(venue.getName()));
        if (hasReadableAddress(venue)) {
            block.append("<br/><span style=\"font-size:14px;color:#7b8794;\">")
                    .append(escapeHtml(venue.getAddress().trim()))
                    .append("</span>");
        }
        String mapUrl = buildMapUrl(venue);
        if (mapUrl != null) {
            block.append("<br/><a href=\"").append(escapeHtml(mapUrl))
                    .append("\" style=\"display:inline-block;margin-top:12px;padding:8px 16px;")
                    .append("border:1px solid #0b8043;border-radius:6px;color:#0b8043;")
                    .append("text-decoration:none;font-size:14px;font-weight:bold;\">")
                    .append("&#128205; Open in Google Maps</a>");
        }
        return block.toString();
    }

    /**
     * Venue text is admin-entered and map links carry {@code &} between query parameters, so both are
     * escaped before they land in the markup.
     */
    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
