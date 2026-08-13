package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Role;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.security.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

import static com.bjit.royalclub.royalclubfootball.security.util.SecurityUtil.getLoggedInUser;

/**
 * Rate-limits profile photo changes to one per rolling 30 days.
 * <p>
 * The limit exists to protect a free-tier Cloudflare R2 bucket, so it is enforced where the cost is
 * actually incurred - at the moment a presigned upload URL is handed out - rather than when the new
 * key is saved. Checking at save time would let someone upload repeatedly and simply never save,
 * which is the case that fills the bucket.
 * <p>
 * Rolling rather than calendar-month, matching the password-reset quota: a calendar month would let
 * someone change on the 31st and again on the 1st.
 */
@Service
@RequiredArgsConstructor
public class PlayerPhotoQuotaService {

    private static final Set<String> UNLIMITED_ROLES = Set.of("ADMIN", "SUPERADMIN");
    private static final Set<String> UNLIMITED_AUTHORITIES = Set.of("ROLE_ADMIN", "ROLE_SUPERADMIN");

    @Value("${player.photo.change-window-days:30}")
    private long changeWindowDays;

    /**
     * Throws unless the signed-in player may start a photo change now.
     *
     * @return the player the upload is for, so the caller need not look them up again.
     */
    public Player assertCanChangePhoto() {
        UserPrinciple principal = getLoggedInUser();
        Player player = principal.getPlayer();

        // Admins and superadmins are exempt: they change other members' photos to remove something
        // inappropriate, and a quota that blocks moderation is worse than the storage it saves.
        //
        if (hasUnlimitedAuthority(principal)) {
            return player;
        }

        // First photo is free. This doubles as the retry path - a player whose upload failed still
        // has no photo, so they are not locked out of a change they never actually made.
        if (player.getPhotoKey() == null || player.getPhotoKey().isBlank()) {
            return player;
        }

        LocalDateTime lastChange = player.getPhotoUpdatedAt();
        if (lastChange == null) {
            // Has a photo but has never replaced it - still their first, so still free.
            return player;
        }

        LocalDateTime availableAt = lastChange.plusDays(changeWindowDays);
        if (LocalDateTime.now().isBefore(availableAt)) {
            throw new PlayerServiceException(
                    "You can change your profile photo once every " + changeWindowDays
                            + " days. Try again after " + availableAt.toLocalDate() + ".",
                    HttpStatus.TOO_MANY_REQUESTS);
        }
        return player;
    }

    /**
     * Throws unless the signed-in caller may delete the object behind {@code key} - their own photo,
     * or anyone's if they moderate. Without this a member could delete another member's photo just
     * by knowing its key, and keys travel in every player listing.
     */
    public void assertMayDelete(String key) {
        UserPrinciple principal = getLoggedInUser();
        if (hasUnlimitedAuthority(principal)) {
            return;
        }
        String ownKey = principal.getPlayer().getPhotoKey();
        if (ownKey == null || !ownKey.equals(key)) {
            throw new PlayerServiceException("You can only delete your own profile photo.",
                    HttpStatus.FORBIDDEN);
        }
    }

    /** When this player may next change their photo, or null if they may right now. */
    public LocalDateTime changeAvailableAt(Player player) {
        if (hasUnlimitedRole(player)
                || player.getPhotoKey() == null || player.getPhotoKey().isBlank()
                || player.getPhotoUpdatedAt() == null) {
            return null;
        }
        LocalDateTime availableAt = player.getPhotoUpdatedAt().plusDays(changeWindowDays);
        return LocalDateTime.now().isBefore(availableAt) ? availableAt : null;
    }

    /**
     * Reads the granted authorities rather than {@code player.getRoles()}: the roles collection is
     * LAZY and the principal's Player is detached by the time a controller runs, so touching it here
     * would risk a LazyInitializationException. Authorities are plain strings, already materialised
     * when the principal was built.
     */
    private boolean hasUnlimitedAuthority(UserPrinciple principal) {
        return principal.getAuthorities() != null && principal.getAuthorities().stream()
                .anyMatch(authority -> UNLIMITED_AUTHORITIES.contains(authority.getAuthority()));
    }

    private boolean hasUnlimitedRole(Player player) {
        return player.getRoles() != null && player.getRoles().stream()
                .map(Role::getName)
                .anyMatch(UNLIMITED_ROLES::contains);
    }
}
