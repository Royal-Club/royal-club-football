CREATE TABLE IF NOT EXISTS resource_category
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(120) NOT NULL,
    name_bn          VARCHAR(120) NULL,
    slug             VARCHAR(140) NOT NULL,
    description      VARCHAR(500) NULL,
    icon             VARCHAR(60)  NULL,
    sort_order       INT          NOT NULL DEFAULT 0,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by       BIGINT       NOT NULL,
    created_date     DATETIME     NOT NULL,
    last_modified_by BIGINT       NULL,
    updated_date     DATETIME     NULL DEFAULT NULL,
    CONSTRAINT uc_resource_category_slug UNIQUE (slug)
);

CREATE TABLE IF NOT EXISTS resource
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id      BIGINT       NOT NULL,
    title            VARCHAR(200) NOT NULL,
    title_bn         VARCHAR(200) NULL,
    slug             VARCHAR(220) NOT NULL,
    summary          VARCHAR(500) NULL,
    summary_bn       VARCHAR(500) NULL,
    body             LONGTEXT     NULL,
    body_bn          LONGTEXT     NULL,
    content_type     VARCHAR(30)  NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    cover_image_key  VARCHAR(255) NULL,
    external_url     VARCHAR(500) NULL,
    -- Free-form JSON. Reserved for structured payloads such as interactive
    -- formation coordinates, so adding a formation builder needs no migration.
    metadata         LONGTEXT     NULL,
    is_pinned        BOOLEAN      NOT NULL DEFAULT FALSE,
    sort_order       INT          NOT NULL DEFAULT 0,
    view_count       BIGINT       NOT NULL DEFAULT 0,
    published_at     DATETIME     NULL DEFAULT NULL,
    created_by       BIGINT       NOT NULL,
    created_date     DATETIME     NOT NULL,
    last_modified_by BIGINT       NULL,
    updated_date     DATETIME     NULL DEFAULT NULL,
    CONSTRAINT uc_resource_slug UNIQUE (slug),
    CONSTRAINT fk_resource_category FOREIGN KEY (category_id) REFERENCES resource_category (id),
    INDEX idx_resource_status (status),
    INDEX idx_resource_category (category_id),
    INDEX idx_resource_content_type (content_type)
);

CREATE TABLE IF NOT EXISTS resource_attachment
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id      BIGINT       NOT NULL,
    storage_key      VARCHAR(255) NOT NULL,
    file_name        VARCHAR(255) NULL,
    content_type     VARCHAR(120) NULL,
    size_bytes       BIGINT       NULL,
    kind             VARCHAR(20)  NOT NULL,
    caption          VARCHAR(300) NULL,
    caption_bn       VARCHAR(300) NULL,
    sort_order       INT          NOT NULL DEFAULT 0,
    created_by       BIGINT       NOT NULL,
    created_date     DATETIME     NOT NULL,
    last_modified_by BIGINT       NULL,
    updated_date     DATETIME     NULL DEFAULT NULL,
    CONSTRAINT fk_resource_attachment_resource FOREIGN KEY (resource_id) REFERENCES resource (id) ON DELETE CASCADE,
    INDEX idx_resource_attachment_resource (resource_id)
);

-- Read receipts. Populated from day one so "who has seen the match plan"
-- reporting can be switched on later without backfilling history.
CREATE TABLE IF NOT EXISTS resource_view
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT   NOT NULL,
    player_id   BIGINT   NOT NULL,
    view_count  BIGINT   NOT NULL DEFAULT 1,
    first_viewed_at DATETIME NOT NULL,
    last_viewed_at  DATETIME NOT NULL,
    CONSTRAINT uc_resource_view UNIQUE (resource_id, player_id),
    CONSTRAINT fk_resource_view_resource FOREIGN KEY (resource_id) REFERENCES resource (id) ON DELETE CASCADE,
    CONSTRAINT fk_resource_view_player FOREIGN KEY (player_id) REFERENCES players (id)
);

INSERT INTO resource_category (id, name, name_bn, slug, description, icon, sort_order, is_active, created_by, created_date)
VALUES (1, 'Playing Principles', 'খেলার মূলনীতি', 'playing-principles',
        'How we want to play — the habits every player repeats during a match.', 'BulbOutlined', 1, TRUE, 1, NOW()),
       (2, 'Tactics & Formations', 'কৌশল ও ফরমেশন', 'tactics-formations',
        'Formation plans such as 2-2-2 and 2-3-1, with diagrams and roles.', 'DeploymentUnitOutlined', 2, TRUE, 1,
        NOW()),
       (3, 'Training Drills', 'অনুশীলন', 'training-drills',
        'Warm-ups, passing patterns and finishing drills for practice sessions.', 'ThunderboltOutlined', 3, TRUE, 1,
        NOW()),
       (4, 'Match Instructions', 'ম্যাচ নির্দেশনা', 'match-instructions',
        'Instructions published by coordinators ahead of a specific match.', 'NotificationOutlined', 4, TRUE, 1, NOW()),
       (5, 'Club Documents', 'ক্লাব ডকুমেন্ট', 'club-documents',
        'Forms, schedules and other files the club shares with members.', 'FileTextOutlined', 5, TRUE, 1, NOW()),
       (6, 'Videos', 'ভিডিও', 'videos', 'Match footage, highlights and coaching clips.', 'PlayCircleOutlined', 6, TRUE,
        1, NOW());

INSERT INTO resource (id, category_id, title, title_bn, slug, summary, summary_bn, body, body_bn, content_type, status,
                      is_pinned, sort_order, published_at, created_by, created_date)
VALUES (1, 1, 'Playing Principles — 6v6 Futsal', 'খেলার মূলনীতি — ৬ঃ৬ ফুটসাল', 'playing-principles-6v6-futsal',
        'The ten habits that decide how well we play together. Read this before every match.',
        'দশটি অভ্যাস যা নির্ধারণ করে আমরা একসাথে কতটা ভালো খেলি। প্রতিটি ম্যাচের আগে পড়ুন।',
        '## 1. Control → Scan → Pass

- First receive the ball with control.
- Immediately scan for free space and teammates.
- Make a quick, accurate pass (avoid holding the ball unnecessarily).

## 2. Pass and Move

- Never stand still after passing.
- Move into open space to create a new passing option.
- Keep the team in constant motion.

## 3. Support the Ball

- The player with the ball should always have at least two passing options.
- Stay at proper angles instead of hiding behind defenders.

## 4. One- and Two-Touch Football

- Use one-touch or two-touch passes whenever possible.
- Faster ball movement is usually more effective than fast dribbling.

## 5. Play Into Space, Not Feet

- Pass into the space where your teammate is running.
- Encourage forward movement and quick attacks.

## 6. Create Width and Depth

- Spread out when attacking.
- Do not let multiple players crowd the same area.
- This opens passing lanes and creates space.

## 7. Defend Together

- As soon as possession is lost, the nearest player presses the ball.
- Everyone else quickly marks passing lanes and gets behind the ball.

## 8. Communicate Constantly

Use simple calls like:

- **"Man on!"**
- **"Turn!"**
- **"One-two!"**
- **"Time!"**
- **"Switch!"**

## 9. Do Not Force Risky Passes

- If a forward pass is not available, recycle the ball.
- Keeping possession is often better than losing it.

## 10. Shoot Early When the Chance Is There

- In futsal, goalkeepers have very little reaction time.
- Do not over-dribble inside the attacking third.',
        NULL, 'ARTICLE', 'PUBLISHED', TRUE, 1, NOW(), 1, NOW()),

       (2, 1, 'Three Golden Rules', 'তিনটি সোনালি নিয়ম', 'three-golden-rules',
        'If you remember nothing else, remember these three.',
        'আর কিছু মনে না থাকলেও এই তিনটি মনে রাখুন।',
        '1. **The ball moves faster than any player.**
2. **After every pass, find a new space.**
3. **When possession is lost, everyone defends immediately.**',
        '১. **বল যেকোনো খেলোয়াড়ের চেয়ে দ্রুত চলে।**
২. **প্রতিটি পাসের পরে নতুন জায়গা খুঁজে নিন।**
৩. **বল হারালে সবাই সঙ্গে সঙ্গে রক্ষণে নামবে।**',
        'ARTICLE', 'PUBLISHED', TRUE, 2, NOW(), 1, NOW()),

       (3, 1, 'The Team Cycle', 'টিম সাইকেল', 'the-team-cycle',
        'Receive → Scan → Decide → Pass → Move → Support. Every player repeats this cycle throughout the match.',
        'রিসিভ → স্ক্যান → সিদ্ধান্ত → পাস → মুভ → সাপোর্ট। প্রতিটি খেলোয়াড় পুরো ম্যাচ জুড়ে এই চক্রটি চালিয়ে যাবে।',
        'A simple team rule that is easy to remember:

> **Receive → Scan → Decide → Pass → Move → Support**

Every player should repeat this cycle throughout the match.

| Step | What it means |
| --- | --- |
| Receive | Take the first touch under control, away from pressure. |
| Scan | Look up before the ball arrives — know your options early. |
| Decide | Pick the option before the ball reaches your foot. |
| Pass | Quick and accurate, into space where possible. |
| Move | Never admire the pass — go somewhere useful. |
| Support | Give the new ball carrier at least two options. |',
        'সহজে মনে রাখার মতো একটি নিয়ম:

> **রিসিভ → স্ক্যান → সিদ্ধান্ত → পাস → মুভ → সাপোর্ট**

প্রতিটি খেলোয়াড় পুরো ম্যাচ জুড়ে এই চক্রটি চালিয়ে যাবে।',
        'ARTICLE', 'PUBLISHED', TRUE, 3, NOW(), 1, NOW());
