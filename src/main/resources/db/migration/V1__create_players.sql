CREATE TABLE IF NOT EXISTS players
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL UNIQUE,
    employee_id      VARCHAR(255) NOT NULL UNIQUE,
    skype_id         VARCHAR(255) NOT NULL UNIQUE,
    mobile_no        VARCHAR(255),
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    playing_position VARCHAR(50)  NOT NULL,
    password         VARCHAR(255) NOT NULL,
    created_by       BIGINT       NOT NULL,
    created_date     DATETIME     NOT NULL,
    last_modified_by BIGINT,
    updated_date     DATETIME              DEFAULT NULL,
    CONSTRAINT UC_Player UNIQUE (email, employee_id)
);



/* Insert existing players with passwords */
/* Here employee_id are added for Amir Tushar, Amir Hossain, Sajidul */
INSERT INTO players (id, name, email, employee_id, skype_id, mobile_no, is_active, playing_position, created_by, created_date,
                     last_modified_by, updated_date, password)
VALUES
    (1, 'Golam Sarower', 'sarower.bjitgroup@gmail.com', '10051', 'golam.sarower', NULL, TRUE, 'STRIKER', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (2, 'Rezaul Karim (Admin)', 'mdrezaul.karim0505@gmail.com', '10797', 'live:b054bffc5b3a6011', NULL, TRUE, 'STRIKER', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (3, 'Aktaruzzaman Rakib', 'rakibccj@gmail.com', '11305', 'live:.cid.74cf98961185b8c0', NULL, TRUE, 'STRIKER', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (4, 'Arefin Newaz Prince', 'anprince.bjit@gmail.com', '11377', 'live:.cid.77b7311c62152cbc', NULL, TRUE, 'GOALKEEPER', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (5, 'Rejaul Karim', 'rktirtho@gmail.com', '11256', 'live:.cid.a46364423a22e650', NULL, TRUE, 'GOALKEEPER', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (6, 'Shaharin Ahmed', 'ahmedshaharin7589@gmail.com', '11207', 'live:.cid.64b7e5f8f6c6ca1b', NULL, TRUE, 'CENTRAL_MIDFIELD', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (7, 'Liton', 'liton.bjit@gmail.com', '11703', 'live:.cid.822443dcd8b52ec6', NULL, TRUE, 'LEFT_BACK', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (8, 'Md. Shofiuddin', 'shafi.2013@gmail.com', '11020', 'live:.cid.c6895d48a976f4d8', NULL, TRUE, 'UNASSIGNED', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (9, 'Alamgir', 'alamgir.khan1616@gmail.com', '10855', 'live:.cid.3099ba2947223228', NULL, TRUE, 'CENTRAL_MIDFIELD', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (10, 'Tushar Hasan', 'temptushar1@gmail.com', '00000', 'tusar.nihar', NULL, TRUE, 'CENTRAL_MIDFIELD', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (11, 'Md. Badrul Alam', 'hridoyjbd@gmail.com', '11114', 'hridoyjbd', NULL, TRUE, 'UNASSIGNED', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (12, 'Pranab Sarker', 'pranab.sarkerbjitgroup@gmail.com', '11320', 'live:.cid.ae048a80688066c4', NULL, TRUE, 'UNASSIGNED', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (13, 'Zahidul', 'zahid.bjit@gmail.com', '11156', 'live:jahid.cse84.ji', NULL, TRUE, 'UNASSIGNED', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (14, 'Md. Josim Uddin Roni', 'josim.uddin@bjitgroup.com', '11710', 'live:.cid.cdb585b65e384f59', NULL, TRUE, 'ATTACKING_MIDFIELD', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (15, 'Nahid', 'nahidnaim41@gmail.com', '11641', 'live:.cid.d00dd4a150312711', NULL, TRUE, 'RIGHT_BACK', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (16, 'Shaiful Islam Palash', 'kuvic16@gmail.com', '11466', 'islam.shaiful@bjitgroup.com', NULL, TRUE, 'UNASSIGNED', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (17, 'Sunipoon', 'sunip.ete@gmail.com', '11491', 'live:.cid.7d49ffef8ded8281', NULL, TRUE, 'RIGHT_BACK', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (18, 'Sajidul Huq Rubon', 'sajidul.huq@bjitgroup.com', '00001', 'sajidul', NULL, TRUE, 'RIGHT_BACK', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (19, 'M. Tanvir Khan', 'mtanvirkhan@live.com', '10959', 'live:mtanvirkhan', NULL, TRUE, 'RIGHT_BACK', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (20, 'Gazi Md Saiful Hoque', 'saifulhoque30@gmail.com', '11425', 'live:.cid.c78cc8de401dbd96', NULL, TRUE, 'ATTACKING_MIDFIELD', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (21, 'Amir Hamza', 'a.hamza.cse@gmail.com', '11052', 'live:hamza.cse_1', NULL, TRUE, 'RIGHT_WING_FORWARD', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (22, 'Moniruzzaman Sumon', 'msumon.bjit@gmail.com', '11176', 'live:.cid.d385f2de972484c4', NULL, TRUE, 'LEFT_BACK', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (23, 'Amir Hossain (DevOps)', 'amir.shohag@bjitgroup.com', '00002', 'live:.cid.322950f6bfa8dd7b', NULL, TRUE, 'RIGHT_WING_FORWARD', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si'),
    (24, 'Fazle Mubin', 'fzmubin98@gmail.com', '11729', 'live:.cid.f710ad5fbc37fefa', NULL, TRUE, 'ATTACKING_MIDFIELD', 1, NOW(), NULL, NOW(), '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si');
