package com.bjit.royalclub.royalclubfootball.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Builds the URL-safe identifiers used to address resources and categories.
 * <p>
 * Slugs are derived from the English title only. Bangla titles collapse to an
 * empty string here, which is why callers pass a fallback.
 */
public final class SlugUtil {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_DASHES = Pattern.compile("^-+|-+$");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}");
    private static final int MAX_LENGTH = 180;

    private SlugUtil() {
    }

    public static String toSlug(String input, String fallback) {
        if (input == null || input.isBlank()) {
            return fallback;
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = DIACRITICS.matcher(normalized).replaceAll("");
        String slug = NON_ALPHANUMERIC.matcher(normalized.toLowerCase(Locale.ENGLISH)).replaceAll("-");
        slug = EDGE_DASHES.matcher(slug).replaceAll("");
        if (slug.length() > MAX_LENGTH) {
            slug = EDGE_DASHES.matcher(slug.substring(0, MAX_LENGTH)).replaceAll("");
        }
        return slug.isBlank() ? fallback : slug;
    }

    /**
     * Appends {@code -2}, {@code -3} … until {@code taken} stops matching.
     */
    public static String uniqueSlug(String base, Predicate<String> taken) {
        if (!taken.test(base)) {
            return base;
        }
        int suffix = 2;
        String candidate = base + "-" + suffix;
        while (taken.test(candidate)) {
            suffix++;
            candidate = base + "-" + suffix;
        }
        return candidate;
    }
}
