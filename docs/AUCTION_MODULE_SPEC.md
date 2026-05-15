# Player Auction System — Full Specification

## Overview

A Player Auction Module for Royal Club Football tournament management application.  
Both regular club players and outside company players can participate in auction-based tournaments.  
This module is **additive** — it does not modify or break any existing tournament/player/team behavior.

---

## Existing Application Context

The application already has:

- Player management (BJIT employees with employeeId, skypeId, email, etc.)
- Player roles/positions (FootballPosition enum)
- Contribution system (monthly collections, vouchers)
- Tournament management (ROUND_ROBIN, KNOCKOUT_SINGLE, KNOCKOUT_DOUBLE, GROUP_STAGE_KNOCKOUT, CUSTOM)
- Team management (Team + TeamPlayer entities)
- Tournament participants
- Player statistics (MatchStatistics, PlayerGoalkeepingHistory)
- Role-based JWT authentication (SUPERADMIN, ADMIN, PLAYER, COACH, MANAGER, SPECTATOR, COORDINATOR)
- Flyway migrations (V1–V35)
- MySQL 8, Spring Boot 3.4.4, Java 21
- React 18, TypeScript, Redux Toolkit + RTK Query, Ant Design

---

## Key Design Decisions

### 1. Tournament Auction Flag: Boolean `auction_mode`

Do NOT change the existing `TournamentType` enum.

Add a boolean column `auction_mode` (default `FALSE`) to the `tournament` table.

Any tournament type (ROUND_ROBIN, KNOCKOUT_SINGLE, etc.) can optionally have auction mode enabled.  
When `auction_mode = true`, the auction module is activated for that tournament.  
When `auction_mode = false`, tournament works exactly as before.

### 2. Outside Players: Same Company Employees

Outside players are BJIT employees who are not regular Royal Club members.  
They have all required fields: `employeeId`, `skypeId`, `email`, `name`, `mobileNo`, etc.  
They come specifically to participate in an auction tournament.

After admin approval, they are inserted into the `players` table with role `OUTSIDE_PLAYER`.  
They use the same login endpoint, same JWT auth, same `CustomUserDetailsService`.

Later, admin can promote them to regular `PLAYER` role if they want to join the club permanently and pay contribution.

### 3. Access Scoping by Role

- `OUTSIDE_PLAYER` role can ONLY access tournaments where `auctionMode = true`
- Existing `PLAYER` role keeps all current access unchanged + can also see auction tournaments
- `TEAM_OWNER` role is assigned to players who own a team in an auction tournament
- A player can have multiple roles (e.g., `PLAYER` + `TEAM_OWNER`)

### 4. Public Auction Dashboard

Anyone with access to the application (including `SPECTATOR`, `OUTSIDE_PLAYER`, `PLAYER`) can view the **Auction Dashboard** for any active auction tournament.  
This is a read-only public view — no admin controls, no bid buttons, no private team data.

---

## Player Types and Access Rules

### Regular/Internal Player (Role: PLAYER)

Existing club members. No change to their permissions.

Can:
- Log in normally
- See all tournaments (regular + auction)
- Join/participate as before
- Access existing dashboard, contribution, statistics
- View auction dashboard if they are part of an auction tournament
- Be added to auction player pool by admin

Cannot:
- Bid in auction (unless they also have TEAM_OWNER role)

### Outside Auction Player (Role: OUTSIDE_PLAYER)

Same company employees who are not regular club members.

Registration flow:
1. Outside player submits registration request for a specific auction tournament
2. Admin/Super Admin reviews and approves
3. Upon approval, player record created in `players` table with role `OUTSIDE_PLAYER`
4. Player can now log in

Can:
- Log in after approval
- See tournaments where `auctionMode = true` only
- View auction dashboard for those tournaments
- See their own auction status (approved, in pool, sold, unsold, assigned team)
- View public auction information

Cannot:
- See regular (non-auction) tournaments
- Access contribution system
- Access regular player dashboard features
- Join regular tournaments
- Bid in auction (unless also assigned TEAM_OWNER role)

### Team Owner (Role: TEAM_OWNER)

An existing player (regular or outside) assigned as team owner for an auction tournament.

Can:
- Bid in live auction for their assigned team
- See their team budget, bought players, required roles
- Access owner dashboard during auction
- View auction dashboard

Cannot:
- Bid for other teams
- Bid more than remaining budget
- Exceed squad limits
- Access admin auction controls

### Admin / Super Admin (Role: ADMIN, SUPERADMIN)

Full auction control.

Can:
- Enable auction mode on tournaments
- Configure auction settings
- Approve/reject outside player registrations
- Manage auction player pool
- Create teams, assign owners, set budgets
- Start/pause/resume/end auction
- Control which player is on auction
- Mark sold/unsold, undo actions
- View all dashboards and reports

---

## Auction Tournament Flow

```
Create Tournament
→ Enable auction_mode = true
→ Configure Auction Settings (budget, timer, bid increment, squad rules)
→ Create Teams + Assign Team Owners + Set Budgets
→ Open Outside Player Registration
→ Outside Players Register
→ Admin Approves Outside Players
→ Admin Adds Existing Players + Approved Outside Players to Auction Pool
→ Admin Sets Player Base Prices and Categories
→ Players Confirm Availability
→ Admin Starts Live Auction
→ One Player Appears on Auction Screen
→ Team Owners Bid (real-time via WebSocket)
→ Timer Ends → Player Sold or Unsold
→ Next Player
→ After All Players: Unsold Round (optional)
→ Final Team Squads Generated
→ Tournament Starts (regular tournament flow takes over)
```

---

## Auction Settings

Per-tournament configuration. Created by Admin/Super Admin.

| Field | Type | Description |
|-------|------|-------------|
| tournament_id | FK | Which tournament |
| team_budget | Integer | Budget each team gets (e.g., 10000) |
| min_squad_size | Integer | Minimum players per team (e.g., 10) |
| max_squad_size | Integer | Maximum players per team (e.g., 15) |
| auction_timer_seconds | Integer | Timer per player (e.g., 120 or 180) |
| bid_increment | Integer | Minimum bid step (e.g., 100) |
| unsold_reauction_enabled | Boolean | Allow unsold players to return |
| timer_extension_seconds | Integer | How much to extend on late bid (e.g., 15) |
| extend_if_bid_within_last_seconds | Integer | Late bid window (e.g., 15) |
| min_role_requirements | JSON | e.g., {"GOALKEEPER": 1, "DEFENDER": 3, "MIDFIELDER": 3, "FORWARD": 2} |
| auction_status | Enum | NOT_STARTED, REGISTRATION_OPEN, POOL_READY, LIVE, PAUSED, COMPLETED |

---

## Outside Player Registration

### Registration Request Table: `auction_player_registrations`

This holds pending registration requests BEFORE approval.

| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| tournament_id | FK | Which auction tournament |
| name | String | Full name |
| email | String | Company email |
| employee_id | String | BJIT employee ID |
| skype_id | String | Skype ID |
| mobile_no | String | Phone |
| playing_position | Enum | FootballPosition |
| batting_style | String | Optional (for cricket) |
| bowling_style | String | Optional (for cricket) |
| previous_experience | Text | Description |
| availability_status | Enum | AVAILABLE, PARTIALLY_AVAILABLE, NOT_AVAILABLE |
| profile_photo | String | File path/URL |
| emergency_contact | String | Optional |
| preferred_base_price | Integer | Player's suggested price (admin can override) |
| approval_status | Enum | PENDING, APPROVED, REJECTED |
| approved_by | FK | Admin who approved |
| approved_at | DateTime | When approved |
| rejection_reason | String | If rejected |
| created_player_id | FK | After approval, references the created player record |
| created_at | DateTime | |
| updated_at | DateTime | |

### Approval Flow

```
Player submits registration → approval_status = PENDING
Admin reviews → APPROVED or REJECTED
If APPROVED:
  → Create record in `players` table (name, email, employeeId, skypeId, mobileNo, position, default password)
  → Assign role OUTSIDE_PLAYER in players_roles
  → Set created_player_id on the registration record
  → Player can now log in
  → Player auto-added to auction pool (or admin adds manually)
If REJECTED:
  → Set rejection_reason
  → Player cannot log in
```

---

## Auction Player Pool

### Table: `auction_players`

Contains all players eligible for auction in a tournament.

| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| tournament_id | FK | Tournament |
| player_id | FK | References `players.id` (works for both existing and approved outside players) |
| player_type | Enum | EXISTING, OUTSIDE |
| category | Enum | ICON, A_GRADE, B_GRADE, EMERGING, OUTSIDE |
| base_price | Integer | Starting bid price |
| current_bid | Integer | Latest bid (null if not yet auctioned) |
| current_highest_team_id | FK | Team with highest bid |
| sold_to_team_id | FK | Final sold team (null if unsold) |
| final_price | Integer | Final sale price |
| status | Enum | AVAILABLE, ON_AUCTION, SOLD, UNSOLD, WITHDRAWN |
| auction_round | Integer | 1 = first round, 2 = unsold round, etc. |
| player_rating | Decimal | Optional computed rating |
| sequence_order | Integer | Order in which player appears |
| created_at | DateTime | |
| updated_at | DateTime | |

### Player Statuses

- `AVAILABLE` — In pool, waiting for their turn
- `ON_AUCTION` — Currently being auctioned (on screen)
- `SOLD` — Bought by a team
- `UNSOLD` — No bids received, timer expired
- `WITHDRAWN` — Removed from pool by admin or player

---

## Team Owner Budget System

### Table: `team_budgets`

| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| tournament_id | FK | Tournament |
| team_id | FK | References `team.id` |
| owner_id | FK | References `players.id` (the team owner) |
| total_budget | Integer | Starting budget |
| remaining_budget | Integer | Budget left after purchases |
| total_spent | Integer | Total spent on players |
| players_bought | Integer | Count of bought players |
| created_at | DateTime | |
| updated_at | DateTime | |

### Budget Rules

- Owner cannot bid more than `remaining_budget`
- Owner cannot bid below `base_price` (first bid) or below `current_bid + bid_increment` (subsequent bids)
- Owner cannot exceed `max_squad_size`
- Budget deducted ONLY when player is officially `SOLD`
- System should warn if remaining budget makes it impossible to fill minimum squad

---

## Live Auction Session

### Table: `auction_sessions`

| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| tournament_id | FK | Tournament |
| status | Enum | NOT_STARTED, RUNNING, PAUSED, COMPLETED |
| current_auction_player_id | FK | Player currently on screen |
| round_number | Integer | Current round (1, 2, etc.) |
| started_at | DateTime | When auction started |
| paused_at | DateTime | When paused |
| completed_at | DateTime | When ended |
| current_timer_ends_at | DateTime | Server-controlled timer deadline |
| created_at | DateTime | |
| updated_at | DateTime | |

### Session Statuses

- `NOT_STARTED` — Auction configured but not begun
- `RUNNING` — Live, accepting bids
- `PAUSED` — Temporarily stopped by admin
- `COMPLETED` — All players auctioned, auction over

---

## Bidding System

### Table: `auction_bids`

| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| auction_session_id | FK | Session |
| tournament_id | FK | Tournament |
| auction_player_id | FK | Which player being bid on |
| team_id | FK | Which team is bidding |
| bidder_user_id | FK | Who placed the bid (owner) |
| bid_amount | Integer | Bid value |
| bid_time | DateTime | Exact timestamp |
| is_winning | Boolean | Is this the winning bid |
| created_at | DateTime | |

### Bidding Rules

1. Auction session must be `RUNNING`
2. Player must be `ON_AUCTION`
3. Bidder must have `TEAM_OWNER` role for this tournament
4. Team must belong to this tournament
5. Bid amount ≥ base_price (if first bid)
6. Bid amount ≥ current_bid + bid_increment (if subsequent bid)
7. Team remaining_budget ≥ bid_amount
8. Team players_bought < max_squad_size
9. Backend is the **source of truth** — frontend never decides validity

### Timer Logic

- Timer starts when admin puts a player `ON_AUCTION`
- Timer is **server-controlled** (stored as `current_timer_ends_at` timestamp)
- Server broadcasts remaining time to all clients via WebSocket
- If timer expires with no bids → player = `UNSOLD`
- If timer expires with at least one bid → player = `SOLD` to highest bidder
- If a bid arrives within last N seconds (configurable) → timer extends by N seconds
- This prevents unfair last-second sniping

### Sold Logic

When timer ends with valid bid(s):
1. Player status → `SOLD`
2. `sold_to_team_id` = highest bidding team
3. `final_price` = winning bid amount
4. Team's `remaining_budget` deducted by final_price
5. Team's `total_spent` increased by final_price
6. Team's `players_bought` incremented
7. Winning bid's `is_winning` = true
8. Player added to team's squad (via `team_player` table)
9. WebSocket broadcasts SOLD event to all clients

### Unsold Logic

When timer ends with zero bids:
1. Player status → `UNSOLD`
2. Player moves to unsold list
3. WebSocket broadcasts UNSOLD event
4. In unsold round, admin can bring player back with same or reduced base price

---

## Auction Dashboard (Public View)

### Purpose

A **read-only, real-time dashboard** accessible to anyone with application access.  
Shows the complete live auction state without any admin controls or private team data.

### Route: `/tournaments/:id/auction/dashboard`

### Who Can View

| Role | Access |
|------|--------|
| SUPERADMIN | Yes |
| ADMIN | Yes |
| PLAYER | Yes |
| OUTSIDE_PLAYER | Yes (for auction tournaments only) |
| TEAM_OWNER | Yes |
| SPECTATOR | Yes |
| COORDINATOR | Yes |
| Unauthenticated | No (login required) |

### Dashboard Sections

#### A. Auction Status Bar
- Tournament name
- Auction status (Not Started / Live / Paused / Completed)
- Current round number
- Total players in pool
- Players sold / unsold / remaining

#### B. Current Player On Auction (Hero Section)
- Player photo
- Player name
- Player type (Existing / Outside)
- Playing position/role
- Category (Icon / A-Grade / B-Grade / Emerging / Outside)
- Base price
- Current highest bid
- Current highest bidding team
- Live countdown timer (real-time)
- Player stats/rating (if available)
- Bid history for current player (scrollable list of all bids with team name, amount, time)

#### C. Team Budget Overview
- All teams listed with:
  - Team name
  - Owner name
  - Total budget
  - Remaining budget
  - Players bought count
  - Remaining slots

#### D. Recently Sold Players
- Last 5-10 sold players with:
  - Player name
  - Sold to team
  - Final price
  - Category/role

#### E. Unsold Players
- List of unsold players (name, position, base price)

#### F. Team Squads (Live Building)
- Each team's current squad as players are bought:
  - Player name
  - Position
  - Purchase price
- Shows role balance (how many batsmen, bowlers, etc.)

#### G. Auction Statistics (Live)
- Most expensive player so far
- Average sale price
- Total money spent across all teams
- Most active bidding team
- Highest bid war (most bids on single player)

### Real-time Updates

The dashboard connects via WebSocket and updates automatically when:
- New bid placed
- Timer ticks
- Player sold
- Player unsold
- Next player starts
- Auction paused/resumed/ended
- Team budget changes

### Public Display Mode (Projector/TV)

Route: `/tournaments/:id/auction/display`

Same data as dashboard but:
- Larger fonts, optimized for big screens
- No navigation/sidebar
- Auto-rotating sections
- Sold/unsold animations
- Focus on current player + timer + bids
- No login required if admin enables public display mode (optional future feature)

---

## Admin Controls

### Auction Management Page

Route: `/tournaments/:id/auction/admin`

Admin can:

| Action | Description |
|--------|-------------|
| Start auction | Begin live session |
| Pause auction | Temporarily halt (timer freezes) |
| Resume auction | Continue from pause |
| End auction | Mark auction complete |
| Select next player | Choose specific player from pool |
| Random next player | System picks randomly from available |
| Auto-sequence | Follow pre-set sequence order |
| Skip player | Move current to back of queue |
| Mark sold | Manually confirm sale |
| Mark unsold | Manually mark unsold |
| Undo last sale | Reverse last sold player (return to pool, refund budget) |
| Restart bidding | Clear bids on current player, restart timer |
| Edit base price | Change base price before/during auction |
| Remove player | Withdraw player from pool |
| Start unsold round | Begin re-auction of unsold players |
| Set reduced base price | For unsold round |

---

## Owner Dashboard

### Route: `/tournaments/:id/auction/owner`

Private view for team owners during live auction.

Shows:
- My team name
- My total budget / remaining budget
- Players I've bought (name, position, price)
- Required roles still needed
- Remaining squad slots
- Current player on auction
- Bid buttons (Bid +100, Bid +500, Bid +1000, Custom)
- My bid status (leading / outbid)
- Warning messages (e.g., "You need 2 more bowlers", "Budget too low for minimum squad")

---

## WebSocket / Real-Time Architecture

### Technology

- Backend: Spring Boot WebSocket + STOMP protocol
- Frontend: `@stomp/stompjs` + `sockjs-client`

### Dependencies to Add

Backend (`pom.xml`):
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

Frontend (`package.json`):
```json
"@stomp/stompjs": "^7.0.0",
"sockjs-client": "^1.6.1"
```

### WebSocket Topics (Subscribe)

| Topic | Purpose | Who Subscribes |
|-------|---------|---------------|
| `/topic/auction/{tournamentId}/state` | Auction session status changes (start, pause, resume, end) | All |
| `/topic/auction/{tournamentId}/player` | Current player on auction (new player appears) | All |
| `/topic/auction/{tournamentId}/bid` | New bid placed (amount, team, remaining time) | All |
| `/topic/auction/{tournamentId}/timer` | Timer tick (remaining seconds) | All |
| `/topic/auction/{tournamentId}/sold` | Player sold (player, team, price) | All |
| `/topic/auction/{tournamentId}/unsold` | Player unsold | All |
| `/topic/auction/{tournamentId}/budget` | Team budget updated | All |
| `/topic/auction/{tournamentId}/error` | Bid rejection reason (sent to specific user) | Bidder only |

### WebSocket Destinations (Send)

| Destination | Purpose | Who Sends |
|-------------|---------|-----------|
| `/app/auction/{tournamentId}/bid` | Place a bid | Team Owner |

### Message Payloads

**Bid Update (broadcast):**
```json
{
  "auctionPlayerId": 15,
  "teamId": 3,
  "teamName": "Royal Strikers",
  "bidAmount": 1500,
  "bidderName": "Mr. Hasan",
  "remainingSeconds": 45,
  "bidCount": 7
}
```

**Sold Event (broadcast):**
```json
{
  "auctionPlayerId": 15,
  "playerName": "Md. Rakib",
  "soldToTeamId": 3,
  "soldToTeamName": "Royal Strikers",
  "finalPrice": 1500,
  "teamRemainingBudget": 8500
}
```

**Timer Tick (broadcast every second or every 5 seconds):**
```json
{
  "auctionPlayerId": 15,
  "remainingSeconds": 120,
  "extended": false
}
```

---

## Backend API Endpoints

### Auction Settings
```
GET    /api/tournaments/{tournamentId}/auction/settings
POST   /api/tournaments/{tournamentId}/auction/settings
PUT    /api/tournaments/{tournamentId}/auction/settings
```

### Outside Player Registration
```
POST   /api/auction/tournaments/{tournamentId}/register          (public - outside player registers)
GET    /api/auction/registrations?status=PENDING                 (admin - list registrations)
GET    /api/auction/registrations/{id}                           (admin - view single)
POST   /api/auction/registrations/{id}/approve                   (admin)
POST   /api/auction/registrations/{id}/reject                    (admin)
```

### Auction Player Pool
```
GET    /api/tournaments/{tournamentId}/auction/players
POST   /api/tournaments/{tournamentId}/auction/players/add-existing       (admin adds existing player)
POST   /api/tournaments/{tournamentId}/auction/players/add-from-registration  (admin adds approved outside player)
PUT    /api/tournaments/{tournamentId}/auction/players/{auctionPlayerId}  (update base price, category, order)
DELETE /api/tournaments/{tournamentId}/auction/players/{auctionPlayerId}  (withdraw)
```

### Team Budgets
```
GET    /api/tournaments/{tournamentId}/auction/team-budgets
POST   /api/tournaments/{tournamentId}/auction/team-budgets                (create/assign)
PUT    /api/tournaments/{tournamentId}/auction/team-budgets/{id}           (update budget)
```

### Auction Session Control (Admin only)
```
POST   /api/tournaments/{tournamentId}/auction/session/start
POST   /api/tournaments/{tournamentId}/auction/session/pause
POST   /api/tournaments/{tournamentId}/auction/session/resume
POST   /api/tournaments/{tournamentId}/auction/session/end
POST   /api/tournaments/{tournamentId}/auction/session/next-player
POST   /api/tournaments/{tournamentId}/auction/session/next-player/random
POST   /api/tournaments/{tournamentId}/auction/session/skip-player
POST   /api/tournaments/{tournamentId}/auction/session/mark-sold
POST   /api/tournaments/{tournamentId}/auction/session/mark-unsold
POST   /api/tournaments/{tournamentId}/auction/session/undo-last-sale
POST   /api/tournaments/{tournamentId}/auction/session/restart-bidding
POST   /api/tournaments/{tournamentId}/auction/session/start-unsold-round
```

### Bidding (via REST fallback + WebSocket primary)
```
POST   /api/tournaments/{tournamentId}/auction/bids                        (place bid - also available via WebSocket)
GET    /api/tournaments/{tournamentId}/auction/bids                        (all bids in tournament)
GET    /api/tournaments/{tournamentId}/auction/players/{auctionPlayerId}/bids  (bids for one player)
```

### Auction Dashboard Data
```
GET    /api/tournaments/{tournamentId}/auction/dashboard          (full dashboard state)
GET    /api/tournaments/{tournamentId}/auction/dashboard/current-player
GET    /api/tournaments/{tournamentId}/auction/dashboard/teams
GET    /api/tournaments/{tournamentId}/auction/dashboard/sold
GET    /api/tournaments/{tournamentId}/auction/dashboard/unsold
GET    /api/tournaments/{tournamentId}/auction/dashboard/statistics
```

### Results & Reports
```
GET    /api/tournaments/{tournamentId}/auction/results            (final squads)
GET    /api/tournaments/{tournamentId}/auction/reports            (analytics)
```

---

## Frontend Pages & Routes

| Route | Page | Access |
|-------|------|--------|
| `/auction/register/:tournamentId` | Outside Player Registration Form | Public (no login needed) |
| `/tournaments/:id/auction/settings` | Auction Settings (Admin) | ADMIN, SUPERADMIN |
| `/tournaments/:id/auction/registrations` | Registration Approval (Admin) | ADMIN, SUPERADMIN |
| `/tournaments/:id/auction/players` | Auction Player Pool (Admin) | ADMIN, SUPERADMIN |
| `/tournaments/:id/auction/teams` | Teams & Budgets Setup (Admin) | ADMIN, SUPERADMIN |
| `/tournaments/:id/auction/admin` | Live Auction Admin Controls | ADMIN, SUPERADMIN |
| `/tournaments/:id/auction/owner` | Team Owner Bidding Dashboard | TEAM_OWNER |
| `/tournaments/:id/auction/dashboard` | Public Auction Dashboard | All authenticated users |
| `/tournaments/:id/auction/display` | Projector/TV Display | All (possibly no auth) |
| `/tournaments/:id/auction/results` | Final Results & Squads | All authenticated users |

---

## Database Migrations Plan

All new. No existing table modifications except one safe `ALTER TABLE`.

### V36 — Add auction_mode to tournament
```sql
ALTER TABLE tournament ADD COLUMN auction_mode BOOLEAN NOT NULL DEFAULT FALSE;
```

### V37 — Add new roles
```sql
INSERT INTO roles (name) VALUES ('TEAM_OWNER');
INSERT INTO roles (name) VALUES ('OUTSIDE_PLAYER');
```

### V38 — Create auction_player_registrations
```sql
CREATE TABLE auction_player_registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    employee_id VARCHAR(100) NOT NULL,
    skype_id VARCHAR(100),
    mobile_no VARCHAR(20),
    playing_position VARCHAR(50) NOT NULL,
    batting_style VARCHAR(50),
    bowling_style VARCHAR(50),
    previous_experience TEXT,
    availability_status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    profile_photo VARCHAR(500),
    emergency_contact VARCHAR(100),
    preferred_base_price INT,
    approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by BIGINT,
    approved_at DATETIME,
    rejection_reason VARCHAR(500),
    created_player_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tournament_id) REFERENCES tournament(id),
    FOREIGN KEY (approved_by) REFERENCES players(id),
    FOREIGN KEY (created_player_id) REFERENCES players(id)
);
```

### V39 — Create auction_settings
```sql
CREATE TABLE auction_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL UNIQUE,
    team_budget INT NOT NULL DEFAULT 10000,
    min_squad_size INT NOT NULL DEFAULT 10,
    max_squad_size INT NOT NULL DEFAULT 15,
    auction_timer_seconds INT NOT NULL DEFAULT 180,
    bid_increment INT NOT NULL DEFAULT 100,
    unsold_reauction_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    timer_extension_seconds INT NOT NULL DEFAULT 15,
    extend_if_bid_within_last_seconds INT NOT NULL DEFAULT 15,
    min_role_requirements JSON,
    auction_status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tournament_id) REFERENCES tournament(id)
);
```

### V40 — Create auction_players
```sql
CREATE TABLE auction_players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    player_type VARCHAR(20) NOT NULL,
    category VARCHAR(30),
    base_price INT NOT NULL DEFAULT 500,
    current_bid INT,
    current_highest_team_id BIGINT,
    sold_to_team_id BIGINT,
    final_price INT,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    auction_round INT NOT NULL DEFAULT 1,
    player_rating DECIMAL(4,2),
    sequence_order INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tournament_id) REFERENCES tournament(id),
    FOREIGN KEY (player_id) REFERENCES players(id),
    FOREIGN KEY (current_highest_team_id) REFERENCES team(id),
    FOREIGN KEY (sold_to_team_id) REFERENCES team(id),
    UNIQUE KEY uk_tournament_player (tournament_id, player_id)
);
```

### V41 — Create team_budgets
```sql
CREATE TABLE team_budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    total_budget INT NOT NULL DEFAULT 10000,
    remaining_budget INT NOT NULL DEFAULT 10000,
    total_spent INT NOT NULL DEFAULT 0,
    players_bought INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tournament_id) REFERENCES tournament(id),
    FOREIGN KEY (team_id) REFERENCES team(id),
    FOREIGN KEY (owner_id) REFERENCES players(id),
    UNIQUE KEY uk_tournament_team (tournament_id, team_id)
);
```

### V42 — Create auction_sessions
```sql
CREATE TABLE auction_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    current_auction_player_id BIGINT,
    round_number INT NOT NULL DEFAULT 1,
    started_at DATETIME,
    paused_at DATETIME,
    completed_at DATETIME,
    current_timer_ends_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tournament_id) REFERENCES tournament(id),
    FOREIGN KEY (current_auction_player_id) REFERENCES auction_players(id)
);
```

### V43 — Create auction_bids
```sql
CREATE TABLE auction_bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    auction_session_id BIGINT NOT NULL,
    tournament_id BIGINT NOT NULL,
    auction_player_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    bidder_user_id BIGINT NOT NULL,
    bid_amount INT NOT NULL,
    bid_time DATETIME NOT NULL,
    is_winning BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_session_id) REFERENCES auction_sessions(id),
    FOREIGN KEY (tournament_id) REFERENCES tournament(id),
    FOREIGN KEY (auction_player_id) REFERENCES auction_players(id),
    FOREIGN KEY (team_id) REFERENCES team(id),
    FOREIGN KEY (bidder_user_id) REFERENCES players(id),
    INDEX idx_auction_player_bids (auction_player_id, bid_time),
    INDEX idx_tournament_bids (tournament_id, bid_time)
);
```

---

## Concurrency and Data Safety

### Critical Rules

- **Backend is source of truth** — frontend never decides winner or validity
- Every bid validated inside a database transaction
- Use `@Version` (optimistic locking) on `auction_players` and `team_budgets`
- Use `SELECT ... FOR UPDATE` on team budget row when processing a bid
- Prevent: two teams winning same player, budget going negative, sold player receiving bids
- Timer checked server-side (compare `current_timer_ends_at` with `NOW()`)

### Bid Processing Flow (Backend)

```
1. Receive bid (via WebSocket or REST)
2. START TRANSACTION
3. Lock team_budget row (SELECT FOR UPDATE)
4. Validate: session RUNNING, player ON_AUCTION, timer not expired
5. Validate: bid >= current_bid + increment
6. Validate: team remaining_budget >= bid_amount
7. Validate: team players_bought < max_squad_size
8. Save bid to auction_bids
9. Update auction_player.current_bid and current_highest_team_id
10. If bid within last N seconds: extend timer
11. COMMIT
12. Broadcast bid update via WebSocket
```

---

## Security and Access Control

### Backend Enforcement

```java
// On all auction admin endpoints:
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")

// On bidding endpoints:
@PreAuthorize("hasRole('TEAM_OWNER')")
// + verify owner belongs to the tournament's team

// On dashboard endpoints:
// Allow all authenticated users, but filter tournament list for OUTSIDE_PLAYER
// OUTSIDE_PLAYER can only access tournaments where auction_mode = true

// On registration endpoint:
// Public - no auth required (they're registering)
```

### Frontend Enforcement

```typescript
// In route guards / sidebar:
if (userRoles.includes('OUTSIDE_PLAYER') && !userRoles.includes('PLAYER')) {
  // Only show tournaments with auctionMode = true
  // Hide contribution, regular dashboard, regular tournament features
}
```

---

## MVP Build Order

### Phase 1: Backend Foundation
1. V36 migration: `auction_mode` column on tournament
2. V37 migration: new roles (TEAM_OWNER, OUTSIDE_PLAYER)
3. V38 migration: `auction_player_registrations` table
4. V39 migration: `auction_settings` table
5. V40 migration: `auction_players` table
6. V41 migration: `team_budgets` table
7. V42 migration: `auction_sessions` table
8. V43 migration: `auction_bids` table
9. Add `TEAM_OWNER` and `OUTSIDE_PLAYER` to `PlayerRole` enum
10. Add `auctionMode` field to `Tournament` entity
11. Create all new entity classes
12. Create repositories

### Phase 2: Core APIs
13. Auction settings CRUD
14. Outside player registration + approval flow
15. Auction player pool management
16. Team budget management
17. Auction session control APIs (start/pause/resume/end/next)
18. Bid creation + validation
19. Sold/unsold logic
20. Dashboard data APIs

### Phase 3: Real-Time
21. Add `spring-boot-starter-websocket` dependency
22. WebSocket + STOMP configuration class
23. WebSocket message handlers (bid, timer)
24. Timer service (server-side countdown, scheduled broadcast)
25. Broadcast on bid/sold/unsold events
26. JWT authentication for WebSocket connections

### Phase 4: Frontend
27. WebSocket client setup (`@stomp/stompjs`)
28. Auction RTK Query slices (settings, pool, budgets, session, bids, dashboard)
29. Outside player registration page
30. Admin: registration approval page
31. Admin: auction settings page
32. Admin: player pool management page
33. Admin: team & budget setup page
34. Admin: live auction control page
35. Owner: bidding dashboard
36. **Public: auction dashboard page** (read-only, real-time)
37. Results page

### Phase 5: Enhancements (Post-MVP)
38. Public display mode (projector/TV)
39. Unsold re-auction round
40. Player rating calculation
41. Auto base price suggestion
42. Watchlist/shortlist
43. Auction analytics & reports
44. Sound effects
45. Team balance score
46. Anti-collusion audit log

---

## Acceptance Criteria

The feature is complete when:

- [ ] Admin can enable `auction_mode` on any tournament
- [ ] Outside players can register for auction tournaments
- [ ] Admin can approve/reject registrations
- [ ] Approved outside players can log in and see only auction tournaments
- [ ] Regular players keep all existing access unchanged
- [ ] Admin can configure auction settings
- [ ] Admin can create teams, assign owners, set budgets
- [ ] Admin can add players to auction pool with base prices
- [ ] Admin can start a live auction session
- [ ] Team owners can bid in real-time via WebSocket
- [ ] Backend validates all bids (amount, budget, squad, timer)
- [ ] Timer is server-controlled with extension on late bids
- [ ] Players correctly become SOLD or UNSOLD
- [ ] Team budgets update correctly on sale
- [ ] Sold players appear in team squads
- [ ] Unsold round works correctly
- [ ] **Auction dashboard shows everything live to all users**
- [ ] Bid history is stored and viewable
- [ ] Final results and team summaries display correctly
- [ ] No existing regular tournament behavior is broken
- [ ] Concurrent bids are handled safely (no double-sell, no negative budget)
