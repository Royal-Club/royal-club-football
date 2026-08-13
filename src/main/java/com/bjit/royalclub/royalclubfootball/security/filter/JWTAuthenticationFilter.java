package com.bjit.royalclub.royalclubfootball.security.filter;

import com.bjit.royalclub.royalclubfootball.security.CustomUserDetailsService;
import com.bjit.royalclub.royalclubfootball.security.JwtUnAuthorizedResponseAuthenticationEntryPoint;
import com.bjit.royalclub.royalclubfootball.util.JWTUtil;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.SESSION_EXPIRED;

@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Endpoints whose entire job is to run without a good access token: the caller's credential is
     * in the request body, not the header. They are skipped outright, because a client renewing an
     * expired session will often still be sending the expired token alongside - and rejecting that
     * would make renewal impossible exactly when it is needed.
     */
    private static final Set<String> UNAUTHENTICATED_PATHS =
            Set.of("/auth/login", "/auth/refresh", "/auth/logout");

    private final JWTUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUnAuthorizedResponseAuthenticationEntryPoint unauthorizedEntryPoint;

    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) {
        return UNAUTHENTICATED_PATHS.contains(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        String jwt = getJWTFromRequest(request);
        if (jwt == null) {
            // No credential offered at all. Public endpoints still have to work, so this is not an
            // error - the request simply runs unauthenticated.
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.emailIfValid(jwt);
        if (email == null) {
            // A credential was offered and it is dead. Answering 401 here is the whole point:
            // letting the request continue unauthenticated makes an expired login surface much
            // later as a bewildering 404 or 500 from whichever service asked who the caller was,
            // and no client can tell that apart from a real failure. A 401 tells the client exactly
            // one thing - renew or sign in again.
            rejectAsUnauthorized(request, response);
            return;
        }

        try {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (UsernameNotFoundException e) {
            // The token is intact but the account behind it is gone or deactivated. Same answer as
            // an expired token, so a deactivated member is signed out rather than left in a broken app.
            rejectAsUnauthorized(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void rejectAsUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        unauthorizedEntryPoint.commence(request, response, new BadCredentialsException(SESSION_EXPIRED));
    }

    private String getJWTFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
