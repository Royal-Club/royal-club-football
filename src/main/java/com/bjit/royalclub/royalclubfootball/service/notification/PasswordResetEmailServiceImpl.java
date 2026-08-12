package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PasswordResetEmailServiceImpl implements PasswordResetEmailService {

    private final ClubMailSender clubMailSender;

    @Value("${password-reset.landing-url}")
    private String landingUrl;

    public PasswordResetEmailServiceImpl(ClubMailSender clubMailSender) {
        this.clubMailSender = clubMailSender;
    }

    @Override
    public boolean sendResetLink(Player player, String token, LocalDateTime expiresAt) {
        String subject = "🔑 Reset your Royal Football Club password";
        String description = String.format("password reset link for player %d", player.getId());
        String resetUrl = buildResetUrl(token);
        String validFor = describeLifetime(expiresAt);

        List<Player> delivered = clubMailSender.sendEach(List.of(player), description,
                recipient -> new ClubMailSender.MailContent(subject,
                        buildTextBody(recipient, resetUrl, validFor),
                        buildHtmlBody(recipient, resetUrl, validFor)));

        return !delivered.isEmpty();
    }

    private String buildResetUrl(String token) {
        return UriComponentsBuilder.fromUriString(landingUrl)
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    /**
     * Rendered as a duration rather than a timestamp: the member reads this in their own timezone,
     * and "valid for 1 hour" cannot be misread the way a bare clock time can.
     */
    private String describeLifetime(LocalDateTime expiresAt) {
        long minutes = Math.max(1, Duration.between(LocalDateTime.now(), expiresAt).toMinutes());
        if (minutes < 60) {
            return minutes + " minutes";
        }
        long hours = minutes / 60;
        return hours == 1 ? "1 hour" : hours + " hours";
    }

    private String buildTextBody(Player player, String resetUrl, String validFor) {
        return String.format("""
                        Hi %s,

                        We received a request to reset the password on your club account.

                        Open this link to choose a new password:

                        %s

                        The link is valid for %s and can only be used once.

                        If you did not ask for this, you can ignore this email - your password
                        stays as it is until someone opens the link above.

                        - %s
                        """,
                player.getName(), resetUrl, validFor, clubMailSender.getFromName());
    }

    private String buildHtmlBody(Player player, String resetUrl, String validFor) {
        // Inline styles and a table shell: email clients strip <style> blocks and ignore flex/grid.
        return String.format("""
                        <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6f8;padding:24px 0;">
                          <tr><td align="center">
                            <table width="100%%" cellpadding="0" cellspacing="0" style="max-width:520px;background:#ffffff;border-radius:12px;padding:32px;font-family:Arial,Helvetica,sans-serif;color:#1f2933;">
                              <tr><td style="font-size:16px;">Hi %s,</td></tr>
                              <tr><td style="padding-top:12px;font-size:15px;color:#52606d;">
                                We received a request to reset the password on your club account.
                              </td></tr>
                              <tr><td style="padding-top:28px;">
                                <a href="%s" style="display:inline-block;background:#14213d;color:#ffffff;text-decoration:none;padding:14px 36px;border-radius:6px;font-size:15px;font-weight:bold;">Choose a new password</a>
                              </td></tr>
                              <tr><td style="padding-top:24px;font-size:13px;color:#7b8794;">
                                This link is valid for %s and can only be used once.
                              </td></tr>
                              <tr><td style="padding-top:12px;font-size:13px;color:#7b8794;">
                                If you did not ask for this, you can ignore this email &mdash; your password
                                stays as it is until someone opens the link above.
                              </td></tr>
                              <tr><td style="padding-top:20px;font-size:13px;color:#9aa5b1;">- %s</td></tr>
                            </table>
                          </td></tr>
                        </table>
                        """,
                player.getName(), resetUrl, validFor, clubMailSender.getFromName());
    }
}
