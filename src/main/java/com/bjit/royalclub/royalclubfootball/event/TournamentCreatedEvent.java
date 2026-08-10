package com.bjit.royalclub.royalclubfootball.event;

/**
 * Published once a tournament has been persisted. Consumed after the transaction commits so a mail
 * server hiccup can never roll back the tournament the admin just created.
 */
public record TournamentCreatedEvent(Long tournamentId) {
}
