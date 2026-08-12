/* Optional Google Maps share link per venue, so RSVP emails can offer a tappable "Open in Google Maps".
   Nullable: when it is absent the mail falls back to a Maps search built from the address. */
ALTER TABLE venues
    ADD COLUMN map_url VARCHAR(500) NULL AFTER address;
