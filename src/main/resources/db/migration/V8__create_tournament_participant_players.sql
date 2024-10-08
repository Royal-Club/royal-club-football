CREATE VIEW tournament_participant_players AS
SELECT tournament.id              AS tournament_id,
       tournament.name            AS tournament_name,
       tournament.tournament_date AS tournament_date,
       player.id                  AS player_id,
       player.name                AS player_name,
       player.employee_id         as player_employee_id,
       tp.id                      as tournament_participant_id,
       tp.participation_status    AS participation_status,
       tp.comments                AS comments
FROM tournament tournament
         JOIN
     players player ON player.is_active = TRUE
         LEFT JOIN
     tournament_participant tp ON tp.tournament_id = tournament.id AND tp.player_id = player.id
WHERE tournament.is_active = TRUE;
