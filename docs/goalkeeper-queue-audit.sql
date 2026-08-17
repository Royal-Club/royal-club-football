-- Goalkeeper priority queue - data audit
--
-- The queue ranks players by a ledger: obligation accrued from turning up, minus turns served.
-- The accrual side is only as trustworthy as the attendance and goalkeeping history behind it, and
-- bad history fails silently - it produces plausible-looking numbers rather than an error. Run
-- these before trusting the ranking, and again after any bulk edit of past tournaments.
--
-- MySQL 8+.


-- 1. Duplicate keeper records
-- ---------------------------
-- More than one goalkeeping row for the same player in the same tournament. Each one counts as a
-- turn served, so a duplicate pushes that player down the queue for something they did once.
-- Historically these came from deleting a team without clearing its goalkeeping history and then
-- rebuilding it; both write paths now guard against it, but existing rows need cleaning by hand.
SELECT g.player_id,
       p.name,
       g.tournament_id,
       t.tournament_date,
       COUNT(*) AS rows_recorded
FROM player_goalkeeping_history g
         JOIN players p ON p.id = g.player_id
         JOIN tournament t ON t.id = g.tournament_id
GROUP BY g.player_id, p.name, g.tournament_id, t.tournament_date
HAVING COUNT(*) > 1
ORDER BY t.tournament_date DESC;


-- 2. Tournaments with no keeper recorded
-- -------------------------------------
-- Indistinguishable from a tournament that genuinely needed none. With
-- goalkeeper-queue.estimate-missing-slots=true these accrue the configured default; with it false
-- they accrue nothing. Either way the answer is wrong if the real cause is unentered data, so check
-- whether the recent ones look plausible before leaving the setting alone.
SELECT t.id,
       t.tournament_date,
       t.name,
       (SELECT COUNT(*) FROM tournament_participant tp
         WHERE tp.tournament_id = t.id AND tp.participation_status = TRUE) AS head_count
FROM tournament t
WHERE NOT EXISTS (SELECT 1 FROM player_goalkeeping_history g WHERE g.tournament_id = t.id)
ORDER BY t.tournament_date DESC;


-- 3. Tournaments with no attendance recorded
-- -----------------------------------------
-- These accrue nothing for anybody, because there is no head count to share the work between.
-- A run of them means the ledger silently starts later than you think it does.
SELECT t.id,
       t.tournament_date,
       t.name
FROM tournament t
WHERE NOT EXISTS (SELECT 1 FROM tournament_participant tp
                   WHERE tp.tournament_id = t.id AND tp.participation_status = TRUE)
ORDER BY t.tournament_date DESC;


-- 4. Ordinal drift
-- ---------------
-- player_goalkeeping_history.round_number is a per-player lifetime ordinal that never rewinds when
-- rows are deleted, so it drifts above the real number of turns served. Nothing should read it as a
-- count; this quantifies how far off it would be if something did.
SELECT g.player_id,
       p.name,
       COUNT(*)             AS turns_actually_served,
       MAX(g.round_number)  AS highest_ordinal,
       MAX(g.round_number) - COUNT(*) AS drift
FROM player_goalkeeping_history g
         JOIN players p ON p.id = g.player_id
GROUP BY g.player_id, p.name
HAVING drift <> 0
ORDER BY drift DESC;


-- 5. The ledger itself
-- --------------------
-- Mirrors what the API computes, over all history rather than one tournament, so the ranking can be
-- sanity-checked outside the application. The COALESCE(NULLIF(...), 2) matches
-- estimate-missing-slots=true with default-slots-per-tournament=2 - change the 2 if you retuned it,
-- or drop the COALESCE entirely to see the ledger with estimation off.
--
-- debt > 0 means owed a turn; debt < 0 means already ahead of their share.
WITH tournament_load AS (SELECT t.id                                                     AS tournament_id,
                                (SELECT COUNT(*)
                                 FROM player_goalkeeping_history g
                                 WHERE g.tournament_id = t.id)                           AS gk_slots,
                                (SELECT COUNT(*)
                                 FROM tournament_participant tp
                                 WHERE tp.tournament_id = t.id
                                   AND tp.participation_status = TRUE)                   AS head_count
                         FROM tournament t),
     accrual AS (SELECT tp.player_id,
                        COUNT(*) AS attended,
                        SUM(CASE
                                WHEN tl.head_count > 0
                                    THEN COALESCE(NULLIF(tl.gk_slots, 0), 2) / tl.head_count
                                ELSE 0 END) AS accrued
                 FROM tournament_participant tp
                          JOIN tournament_load tl ON tl.tournament_id = tp.tournament_id
                 WHERE tp.participation_status = TRUE
                 GROUP BY tp.player_id),
     served AS (SELECT player_id, COUNT(*) AS stints
                FROM player_goalkeeping_history
                GROUP BY player_id)
SELECT p.name,
       p.employee_id,
       p.gk_eligible,
       a.attended,
       ROUND(a.accrued, 2)                             AS accrued,
       COALESCE(s.stints, 0)                           AS served,
       ROUND(a.accrued - COALESCE(s.stints, 0), 2)     AS debt
FROM players p
         JOIN accrual a ON a.player_id = p.id
         LEFT JOIN served s ON s.player_id = p.id
WHERE p.is_active = TRUE
ORDER BY debt DESC;
