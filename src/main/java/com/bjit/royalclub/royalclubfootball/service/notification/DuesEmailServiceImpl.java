package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Informational only - dues are settled offline and recorded by an admin, so the email carries no
 * link or button. There is nothing for a member to action on the site.
 */
@Service
public class DuesEmailServiceImpl implements DuesEmailService {

    private static final DateTimeFormatter MONTH_DISPLAY = DateTimeFormatter.ofPattern("MMMM");
    private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ClubMailSender clubMailSender;

    public DuesEmailServiceImpl(ClubMailSender clubMailSender) {
        this.clubMailSender = clubMailSender;
    }

    @Override
    public List<Player> sendDuesEmails(List<Player> players, LocalDate month) {
        String monthName = month.format(MONTH_DISPLAY);
        String subject = String.format("💰 Membership dues for %s are unpaid", monthName);
        String description = String.format("dues reminder for %s", month.format(MONTH_KEY));

        return clubMailSender.sendEach(players, description, player ->
                new ClubMailSender.MailContent(subject,
                        buildTextBody(player, monthName),
                        buildHtmlBody(player, monthName)));
    }

    private String buildTextBody(Player player, String monthName) {
        return String.format("""
                        Hi %s,

                        Your membership dues for %s are unpaid.
                        Please contact the admin / make your payment offline.

                        If you have already paid, please ignore this message - it may not have been recorded yet.

                        - %s
                        """,
                player.getName(), monthName, clubMailSender.getFromName());
    }

    private String buildHtmlBody(Player player, String monthName) {
        // Inline styles and a table shell: email clients strip <style> blocks and ignore flex/grid.
        return String.format("""
                        <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6f8;padding:24px 0;">
                          <tr><td align="center">
                            <table width="100%%" cellpadding="0" cellspacing="0" style="max-width:520px;background:#ffffff;border-radius:12px;padding:32px;font-family:Arial,Helvetica,sans-serif;color:#1f2933;">
                              <tr><td style="font-size:16px;">Hi %s,</td></tr>
                              <tr><td style="padding-top:20px;font-size:17px;">
                                Your membership dues for <strong>%s</strong> are unpaid.
                              </td></tr>
                              <tr><td style="padding-top:12px;font-size:15px;color:#52606d;">
                                Please contact the admin / make your payment offline.
                              </td></tr>
                              <tr><td style="padding-top:24px;font-size:13px;color:#7b8794;">
                                If you have already paid, please ignore this message - it may not have been recorded yet.
                              </td></tr>
                              <tr><td style="padding-top:20px;font-size:13px;color:#9aa5b1;">- %s</td></tr>
                            </table>
                          </td></tr>
                        </table>
                        """,
                player.getName(), monthName, clubMailSender.getFromName());
    }
}
