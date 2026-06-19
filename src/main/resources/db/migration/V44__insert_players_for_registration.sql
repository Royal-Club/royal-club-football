-- Insert player records from provided employee list.
-- Rules applied:
-- 1) skype_id = email
-- 2) is_active = TRUE
-- 3) default password hash applied to all
-- 4) playing_position = UNASSIGNED
-- 5) duplicate-safe insert by email/employee_id/skype_id

INSERT INTO players (
    name,
    email,
    employee_id,
    skype_id,
    mobile_no,
    is_active,
    playing_position,
    password,
    last_password_change_date,
    created_by,
    created_date,
    last_modified_by,
    updated_date
)
SELECT
    src.name,
    src.email,
    src.employee_id,
    src.email AS skype_id,
    NULL AS mobile_no,
    TRUE AS is_active,
    'UNASSIGNED' AS playing_position,
    '$2a$10$L7bmVacjhGGtMD0qtUkXQOF.Xq6lZ/VkV.GNEMVvsbUlYXSIYp9si' AS password,
    NULL AS last_password_change_date,
    1 AS created_by,
    NOW() AS created_date,
    NULL AS last_modified_by,
    NOW() AS updated_date
FROM (
    SELECT '11889' AS employee_id, 'rabiul.islam@bjitgroup.com' AS email, 'Rabiul Islam Sujon' AS name
    UNION ALL SELECT '11034', 's.islam@bjitgroup.com', 'Saiful Islam'
    UNION ALL SELECT '11896', 'islam.amirul@bjitgroup.com', 'Md. Amirul Islam'
    UNION ALL SELECT '11894', 'md.roni@bjitgroup.com', 'Md. Roni'
    UNION ALL SELECT '11704', 'ahasan.khan@bjitgroup.com', 'Ahasan Khan'
    UNION ALL SELECT '10192', 'abu.hossain@bjitgroup.com', 'Abu Hossain'
    UNION ALL SELECT '10633', 'shakhawat.hossin@bjitgroup.com', 'shakhawat'
    UNION ALL SELECT '11425', 'saiful.hoque@bjitgroup.com', 'Gazi (c)'
    UNION ALL SELECT '11664', 'zahid.hasan@bjitgroup.com', 'Zahid'
    UNION ALL SELECT '11656', 'amir.shohag@bjitgroup.com', 'Amir Hossain (Manager)'
    UNION ALL SELECT '11701', 'fahim.faez@bjitgroup.com', 'Md Fahim Faez Abir'
    UNION ALL SELECT '11166', 'abid.hasan@bjitgroup.com', 'Abid Hasan Rashel'
    UNION ALL SELECT '11466', 'islam.shaiful@bjitgroup.com', 'Shaiful Islam [c]'
    UNION ALL SELECT '11685', 'mustafizur.rahman@bjitgroup.com', 'Sheikh Mustafizur Rahman'
    UNION ALL SELECT '11020', 'md.shofiuddin@bjitgroup.com', 'Shofiuddin'
    UNION ALL SELECT '11180', 'benzir.hasan@bjitgroup.com', 'Md. Benzir Hasan'
    UNION ALL SELECT '11228', 'monir.zzaman@bjitgroup.com', 'Monir Zzaman'
    UNION ALL SELECT '11710', 'josim.uddin@bjitgroup.com', 'Md. Josim Uddin Roni'
    UNION ALL SELECT '11108', 'a.masud@bjitgroup.com', 'M. A Masud'
    UNION ALL SELECT '10934', 'sakib.ehsan@bjitgroup.com', 'Sakib Bin Ehsan'
    UNION ALL SELECT '11777', 'abu.taeb@bjitgroup.com', 'Abu Taeb Nuri (C)'
    UNION ALL SELECT '10391', 'sumon.mohtasin@bjitgroup.com', 'Sumon Mohtasin (Manager)'
    UNION ALL SELECT '11923', 'zafor.ullah@bjitgroup.com', 'Zafor Ullah Khan'
    UNION ALL SELECT '11253', 'dibyendu.bachar@bjitgroup.com', 'Dibyendu Bachar'
    UNION ALL SELECT '11903', 'shariar.arafat@bjitgroup.com', 'Arafat'
    UNION ALL SELECT '11906', 'sohel.mia@bjitgroup.com', 'Md. Sohel Mia'
    UNION ALL SELECT '11740', 'mushfiqur.rahman@bjitgroup.com', 'Mushfiqur Rahman'
    UNION ALL SELECT '11072', 'tanzir.ahamed@bjitgroup.com', 'Tanzir Ahamed'
    UNION ALL SELECT '11723', 'habibullah.howlader@bjitgroup.com', 'Habibullah'
    UNION ALL SELECT '10947', 'hossain.nazmul@bjitgroup.com', 'Nazmul Hossain'
    UNION ALL SELECT '10247', 'himadri.mondal@bjitgroup.com', 'HIMADRI MONDAL (C)'
    UNION ALL SELECT '11092', 'shakib.jahan@bjitgroup.com', 'MD. SHAKIB JAHAN(M)'
    UNION ALL SELECT '11152', 'hossain.farhad@bjitgroup.com', 'MD. FARHAD HOSSAIN'
    UNION ALL SELECT '11447', 'hasibul.sakib@bjitgroup.com', 'MD. HASIBUL HASAN SAKIB'
    UNION ALL SELECT '11429', 'adnan.younus@bjitgroup.com', 'MD. ADNAN YOUNUS'
    UNION ALL SELECT '11881', 'tarikul.saykat@bjitgroup.com', 'TARIKUL ISLAM SAYKAT'
    UNION ALL SELECT '11052', 'amir.hamza@bjitgroup.com', 'MD AMIR HAMZA'
    UNION ALL SELECT '11861', 'anowarul.karim@bjitgroup.com', 'BHUYAN MD ANOWARUL KARIM'
    UNION ALL SELECT '11625', 'abir.ahmed@bjitgroup.com', 'ABIR AHMED'
    UNION ALL SELECT '10442', 'shafiqul.islam@bjitgroup.com', 'MD. SHAFIQUL ISLAM'
    UNION ALL SELECT '11653', 'muktadir.hossain@bjitgroup.com', 'Md. Muktadir Hossain (C)'
    UNION ALL SELECT '11438', 'mdmahadi.hasan@bjitgroup.com', 'Md. Mahadi Hasan (M)'
    UNION ALL SELECT '11378', 'kamrul.hasan@bjitgroup.com', 'Md. Kamrul Hasan'
    UNION ALL SELECT '11715', 'nahian.ahmed@bjitgroup.com', 'Nahian Ahmed'
    UNION ALL SELECT '11644', 'saifuddin.rakib@bjitgroup.com', 'Md Saifuddin Rakib'
    UNION ALL SELECT '11926', 'hakimur.rahman@bjitgroup.com', 'Md. Hakimur Rahman'
    UNION ALL SELECT '11716', 'bipul.kumar@bjitgroup.com', 'Bipul Kumar Paul'
    UNION ALL SELECT '11820', 'm.a.wadud@bjitgroup.com', 'M A Wadud'
    UNION ALL SELECT '11813', 'md.maruf@bjitgroup.com', 'Md. Maruf'
    UNION ALL SELECT '11925', 'farhan.masud@bjitgroup.com', 'Md. Farhan-A-Masud'
    UNION ALL SELECT '11883', 'tanveer.hossain@bjitgroup.com', 'TANVEER HOSSAIN (C)'
    UNION ALL SELECT '60088', 'khandoker.nazmul@bjitgroup.com', 'Khandoker Nazmul Islam'
    UNION ALL SELECT '10689', 'mujahid.joaddar@bjitgroup.com', 'Mujahid Joyaddar'
    UNION ALL SELECT '10975', 'islam.tarikul@bjitgroup.com', 'MD. TARIKUL ISLAM'
    UNION ALL SELECT '11481', 'maruf.billal@bjitgroup.com', 'MARUF BILLAL BADAL'
    UNION ALL SELECT '11056', 'sumon.ahmed@bjitgroup.com', 'SUMON AHMED'
    UNION ALL SELECT '60077', 'sabbir.hossen@bjitgroup.com', 'Sabbir Hossen'
    UNION ALL SELECT '11393', 'mahedi.hasan@bjitgroup.com', 'MD MAHEDI HASAN'
    UNION ALL SELECT '11583', 'ezaz.sarder@bjitgroup.com', 'EZAZ SARDER'
    UNION ALL SELECT '11184', 'sabuj.khan@bjitgroup.com', 'SABUJ KHAN'
    UNION ALL SELECT '10404', 'mamun.khan@bjitgroup.com', 'Mamun Ahmed Khan (M)'
    UNION ALL SELECT '11377', 'arefin.newaz@bjitgroup.com', 'Arefin Newaz Prince (C)'
    UNION ALL SELECT '11691', 'raian.faiz@bjitgroup.com', 'Raian Ibn Faiz'
    UNION ALL SELECT '11473', 'md.tahiduzzaman@bjitgroup.com', 'Md. Tahiduzzaman'
    UNION ALL SELECT '10938', 'eftekher.aqib@bjitgroup.com', 'Mohammad Eftekher Ahmed Aqib'
    UNION ALL SELECT '11143', 'ekramul.hoque@bjitgroup.com', 'Md. Akramul Hoque'
    UNION ALL SELECT '11156', 'islam.zahidul@bjitgroup.com', 'Md Zahidul Islam'
    UNION ALL SELECT '11831', 'mahmudul.haque@bjitgroup.com', 'Mahmudul Haque'
    UNION ALL SELECT '11461', 'amit.mondol@bjitgroup.com', 'Amit Mondol'
    UNION ALL SELECT '11690', 'samiun.rahman@bjitgroup.com', 'Samiun Rahman Sizan'
) src
WHERE NOT EXISTS (
    SELECT 1
    FROM players p
    WHERE p.email = src.email
       OR p.employee_id = src.employee_id
       OR p.skype_id = src.email
);

-- Ensure PLAYER role mapping exists for the provided employee list.
INSERT INTO players_roles (player_id, role_id)
SELECT
    p.id AS player_id,
    r.id AS role_id
FROM players p
JOIN roles r
  ON r.name = 'PLAYER'
JOIN (
    SELECT 'rabiul.islam@bjitgroup.com' AS email
    UNION ALL SELECT 's.islam@bjitgroup.com'
    UNION ALL SELECT 'islam.amirul@bjitgroup.com'
    UNION ALL SELECT 'md.roni@bjitgroup.com'
    UNION ALL SELECT 'ahasan.khan@bjitgroup.com'
    UNION ALL SELECT 'abu.hossain@bjitgroup.com'
    UNION ALL SELECT 'shakhawat.hossin@bjitgroup.com'
    UNION ALL SELECT 'saiful.hoque@bjitgroup.com'
    UNION ALL SELECT 'zahid.hasan@bjitgroup.com'
    UNION ALL SELECT 'amir.shohag@bjitgroup.com'
    UNION ALL SELECT 'fahim.faez@bjitgroup.com'
    UNION ALL SELECT 'abid.hasan@bjitgroup.com'
    UNION ALL SELECT 'islam.shaiful@bjitgroup.com'
    UNION ALL SELECT 'mustafizur.rahman@bjitgroup.com'
    UNION ALL SELECT 'md.shofiuddin@bjitgroup.com'
    UNION ALL SELECT 'benzir.hasan@bjitgroup.com'
    UNION ALL SELECT 'monir.zzaman@bjitgroup.com'
    UNION ALL SELECT 'josim.uddin@bjitgroup.com'
    UNION ALL SELECT 'a.masud@bjitgroup.com'
    UNION ALL SELECT 'sakib.ehsan@bjitgroup.com'
    UNION ALL SELECT 'abu.taeb@bjitgroup.com'
    UNION ALL SELECT 'sumon.mohtasin@bjitgroup.com'
    UNION ALL SELECT 'zafor.ullah@bjitgroup.com'
    UNION ALL SELECT 'dibyendu.bachar@bjitgroup.com'
    UNION ALL SELECT 'shariar.arafat@bjitgroup.com'
    UNION ALL SELECT 'sohel.mia@bjitgroup.com'
    UNION ALL SELECT 'mushfiqur.rahman@bjitgroup.com'
    UNION ALL SELECT 'tanzir.ahamed@bjitgroup.com'
    UNION ALL SELECT 'habibullah.howlader@bjitgroup.com'
    UNION ALL SELECT 'hossain.nazmul@bjitgroup.com'
    UNION ALL SELECT 'himadri.mondal@bjitgroup.com'
    UNION ALL SELECT 'shakib.jahan@bjitgroup.com'
    UNION ALL SELECT 'hossain.farhad@bjitgroup.com'
    UNION ALL SELECT 'hasibul.sakib@bjitgroup.com'
    UNION ALL SELECT 'adnan.younus@bjitgroup.com'
    UNION ALL SELECT 'tarikul.saykat@bjitgroup.com'
    UNION ALL SELECT 'amir.hamza@bjitgroup.com'
    UNION ALL SELECT 'anowarul.karim@bjitgroup.com'
    UNION ALL SELECT 'abir.ahmed@bjitgroup.com'
    UNION ALL SELECT 'shafiqul.islam@bjitgroup.com'
    UNION ALL SELECT 'muktadir.hossain@bjitgroup.com'
    UNION ALL SELECT 'mdmahadi.hasan@bjitgroup.com'
    UNION ALL SELECT 'kamrul.hasan@bjitgroup.com'
    UNION ALL SELECT 'nahian.ahmed@bjitgroup.com'
    UNION ALL SELECT 'saifuddin.rakib@bjitgroup.com'
    UNION ALL SELECT 'hakimur.rahman@bjitgroup.com'
    UNION ALL SELECT 'bipul.kumar@bjitgroup.com'
    UNION ALL SELECT 'm.a.wadud@bjitgroup.com'
    UNION ALL SELECT 'md.maruf@bjitgroup.com'
    UNION ALL SELECT 'farhan.masud@bjitgroup.com'
    UNION ALL SELECT 'tanveer.hossain@bjitgroup.com'
    UNION ALL SELECT 'khandoker.nazmul@bjitgroup.com'
    UNION ALL SELECT 'mujahid.joaddar@bjitgroup.com'
    UNION ALL SELECT 'islam.tarikul@bjitgroup.com'
    UNION ALL SELECT 'maruf.billal@bjitgroup.com'
    UNION ALL SELECT 'sumon.ahmed@bjitgroup.com'
    UNION ALL SELECT 'sabbir.hossen@bjitgroup.com'
    UNION ALL SELECT 'mahedi.hasan@bjitgroup.com'
    UNION ALL SELECT 'ezaz.sarder@bjitgroup.com'
    UNION ALL SELECT 'sabuj.khan@bjitgroup.com'
    UNION ALL SELECT 'mamun.khan@bjitgroup.com'
    UNION ALL SELECT 'arefin.newaz@bjitgroup.com'
    UNION ALL SELECT 'raian.faiz@bjitgroup.com'
    UNION ALL SELECT 'md.tahiduzzaman@bjitgroup.com'
    UNION ALL SELECT 'eftekher.aqib@bjitgroup.com'
    UNION ALL SELECT 'ekramul.hoque@bjitgroup.com'
    UNION ALL SELECT 'islam.zahidul@bjitgroup.com'
    UNION ALL SELECT 'mahmudul.haque@bjitgroup.com'
    UNION ALL SELECT 'amit.mondol@bjitgroup.com'
    UNION ALL SELECT 'samiun.rahman@bjitgroup.com'
) src
  ON src.email = p.email
LEFT JOIN players_roles pr
  ON pr.player_id = p.id
 AND pr.role_id = r.id
WHERE pr.player_id IS NULL;
