package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.Player;

import java.time.LocalDateTime;

public interface PasswordResetEmailService {

    /**
     * @return true only if the mail server accepted the message. The caller spends a quota slot on
     * that answer, so a false must never be reported as a send.
     */
    boolean sendResetLink(Player player, String token, LocalDateTime expiresAt);
}
