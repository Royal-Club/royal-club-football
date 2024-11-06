CREATE TABLE IF NOT EXISTS club_rule
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    description      TEXT,
    created_date     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       BIGINT       NOT NULL,
    updated_date     TIMESTAMP    NULL     DEFAULT NULL,
    last_modified_by BIGINT       NULL
);

INSERT INTO club_rule (id, description, created_by, created_date)
VALUES
    (1,  'কেও বল নিয়ে আগ্রসর হবার সময় প্রতিপক্ষের কেও বিপরীত দিক থেকে সরাসরি বলে শর্ট নিতে পারবে না। এতে করে দুই জনই আঘাতপ্রাপ্ত হতে পারে।', 1, NOW()),
    (2,  'বল দখলের সময় বা  হট্টগোলের মধ্যে কাওকে ধাক্কা দেওয়া যাবে না।', 1, NOW()),
    (3,  'কাউকে ধাক্কা দিয়ে বল দখলের চেষ্টা করা যাবে না।', 1, NOW()),
    (4,  'বলের সাথে দুইজন থাকা অবস্থায় হাটুর উপরে কোন বলে পা উঠানো যাবে না। বলের সাথে বা কাছে অন্য কেও না থাকলে কোন সমস্যা নেই।', 1, NOW()),
    (5,  'কোন সিদ্ধান্তের ব্যাপারে কোন প্রকার বাক বিতন্ডায় জড়ানো যাবে না। ফেয়ার প্লে নিশ্চিত করুন। কোন সিদ্ধান্তের ক্ষেত্রে আনীত অভিযোগকারীরকে প্রতিপক্ষের সিদ্ধান্ত চুড়ান্ত বলে মেনে নিতে হবে।', 1, NOW()),
    (6,  'বল দখলেের সময় উচ্চস্বরে কোন আওয়াজ করা যাবে না।', 1, NOW()),
    (7,  'খেলা চলাকালীন বাইরের কেউ কোন মন্তব্য করতে পারবে না।', 1, NOW()),
    (8,  'আক্রমণাত্মক বা প্রতিশোধমূলক কোন কার্যক্রমে জড়ানো যাবে না।', 1, NOW()),
    (9,  'মাসিক চাদা প্রতি মাসের ৫ তারিখের মধ্যেই প্রদান করতে হবে। প্রতি ১ দিন দেরি করার জন্য ১০০ টাকা হারে অতিরিক্ত চাদা দিতে হবে। এছাড়া অতিরিক্ত বিলম্ব বা নিয়মিত একই ঘটনার জন্য ক্লাব থেকেও বহিষ্কার করা হতে পারে।', 1, NOW()),
    (10, 'যদি কেও হঠাত অসুস্থ্যতার কারনে বা আঘাতপ্রাপ্ত হবার কারনে বা অন্য কোন ব্যাক্তিগত কারনে অংশগ্রহন করতে ব্যার্থ হয় পরবর্তী মাস থেকে মাসিক  চাঁদা দেবার প্রয়োজন নেই। তার জায়গায় অন্য একজন কে সুযোগ দেওয়া হবে।', 1, NOW());

