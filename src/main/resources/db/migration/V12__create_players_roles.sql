CREATE TABLE players_roles
(
    player_id BIGINT,
    role_id   BIGINT,
    PRIMARY KEY (player_id, role_id),
    FOREIGN KEY (player_id) REFERENCES players (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

INSERT INTO players_roles (player_id, role_id)
VALUES (1, 1),  -- Golam Sarower
       (2, 2),  -- Rezaul Karim (Admin)
       (3, 1),  -- Aktaruzzaman Rakib
       (4, 2),  -- Arefin Newaz Prince
       (5, 2),  -- Rejaul Karim
       (6, 2),  -- Shaharin Ahmed
       (7, 2),  -- Liton
       (8, 2),  -- Md. Shofiuddin
       (9, 2),  -- Alamgir
       (10, 2), -- Tushar Hasan
       (11, 1), -- Md. Badrul Alam
       (12, 2), -- Pranab Sarker
       (13, 2), -- Zahidul
       (14, 2), -- Md. Josim Uddin Roni
       (15, 2), -- Nahid
       (16, 2), -- Shaiful Islam Palash
       (17, 2), -- Sunipoon
       (18, 2), -- Sajidul Huq Rubon
       (19, 2), -- M. Tanvir Khan
       (20, 2), -- Gazi Md Saiful Hoque
       (21, 2), -- Amir Hamza
       (22, 2), -- Moniruzzaman Sumon
       (23, 2), -- Amir Hossain (DevOps)
       (24, 2); -- Fazle Mubin
