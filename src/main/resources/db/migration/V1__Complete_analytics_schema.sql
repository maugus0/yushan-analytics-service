-- Analytics Service Database Schema and Initial Data
-- Combined migration file for all analytics tables and data

-- ========================================
-- TABLE CREATIONS
-- ========================================

CREATE TABLE history (
     id SERIAL PRIMARY KEY,
     uuid UUID NOT NULL,
     user_id UUID NOT NULL,
     novel_id INTEGER NOT NULL,
     chapter_id INTEGER NOT NULL,
     create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- INDEXES
-- ========================================

CREATE INDEX IF NOT EXISTS idx_history_user_id ON history(user_id);
CREATE INDEX IF NOT EXISTS idx_history_novel_id ON history(novel_id);
CREATE INDEX IF NOT EXISTS idx_history_chapter_id ON history(chapter_id);
CREATE INDEX IF NOT EXISTS idx_history_create_time ON history(create_time);
CREATE INDEX IF NOT EXISTS idx_history_user_novel ON history(user_id, novel_id);

-- ========================================
-- INITIAL HISTORY DATA
-- ========================================

-- Insert 3000+ history records for reading activity
-- This seed data assumes you have novel and chapter data in your Analytics Service
-- and that user_id UUIDs are synchronized from the User Service

-- Generate realistic reading history with varied patterns
WITH user_activity AS (
    -- Active readers (read many chapters)
    SELECT
        user_id,
        novel_id,
        chapter_id,
        read_count,
        activity_level
    FROM (
        SELECT
            user_id,
            novel_id,
            chapter_id,
            1 as read_count,
            'high' as activity_level
        FROM (
            -- Simulate high-activity users (first 20 non-author users)
            SELECT
                user_id,
                (random() * 50 + 1)::INTEGER as novel_id,
                (random() * 100 + 1)::INTEGER as chapter_id
            FROM (
                SELECT DISTINCT ON (rn)
                    user_id,
                    generate_series(1, 100) as series_num,
                    row_number() OVER () as rn
                FROM (
                    VALUES
                    -- Replace these with actual user UUIDs from your User Service
                    -- These are placeholder UUIDs - you'll need to query your User Service
                    ('user-uuid-1'), ('user-uuid-2'), ('user-uuid-3'),
                    ('user-uuid-4'), ('user-uuid-5'), ('user-uuid-6'),
                    ('user-uuid-7'), ('user-uuid-8'), ('user-uuid-9'),
                    ('user-uuid-10'), ('user-uuid-11'), ('user-uuid-12'),
                    ('user-uuid-13'), ('user-uuid-14'), ('user-uuid-15'),
                    ('user-uuid-16'), ('user-uuid-17'), ('user-uuid-18'),
                    ('user-uuid-19'), ('user-uuid-20')
                ) AS active_users(user_id)
            ) sub
            WHERE rn <= 20
        ) high_activity

        UNION ALL

        -- Simulate medium-activity users (next 30 users)
        SELECT
            user_id,
            (random() * 50 + 1)::INTEGER as novel_id,
            (random() * 100 + 1)::INTEGER as chapter_id
        FROM (
            SELECT DISTINCT ON (rn)
                user_id,
                generate_series(1, 50) as series_num,
                row_number() OVER () as rn
            FROM (
                VALUES
                    ('user-uuid-21'), ('user-uuid-22'), ('user-uuid-23'),
                    ('user-uuid-24'), ('user-uuid-25'), ('user-uuid-26'),
                    ('user-uuid-27'), ('user-uuid-28'), ('user-uuid-29'),
                    ('user-uuid-30'), ('user-uuid-31'), ('user-uuid-32'),
                    ('user-uuid-33'), ('user-uuid-34'), ('user-uuid-35'),
                    ('user-uuid-36'), ('user-uuid-37'), ('user-uuid-38'),
                    ('user-uuid-39'), ('user-uuid-40'), ('user-uuid-41'),
                    ('user-uuid-42'), ('user-uuid-43'), ('user-uuid-44'),
                    ('user-uuid-45'), ('user-uuid-46'), ('user-uuid-47'),
                    ('user-uuid-48'), ('user-uuid-49'), ('user-uuid-50')
                ) AS medium_users(user_id)
            ) sub
            WHERE rn <= 30
        ) medium_activity

        UNION ALL

        -- Simulate low-activity users (remaining users)
        SELECT
            user_id,
            (random() * 50 + 1)::INTEGER as novel_id,
            (random() * 100 + 1)::INTEGER as chapter_id
        FROM (
            SELECT DISTINCT ON (rn)
                user_id,
                generate_series(1, 20) as series_num,
                row_number() OVER () as rn
            FROM (
                VALUES
                    ('user-uuid-51'), ('user-uuid-52'), ('user-uuid-53'),
                    ('user-uuid-54'), ('user-uuid-55'), ('user-uuid-56'),
                    ('user-uuid-57'), ('user-uuid-58'), ('user-uuid-59'),
                    ('user-uuid-60'), ('user-uuid-61'), ('user-uuid-62'),
                    ('user-uuid-63'), ('user-uuid-64'), ('user-uuid-65'),
                    ('user-uuid-66'), ('user-uuid-67'), ('user-uuid-68'),
                    ('user-uuid-69'), ('user-uuid-70'), ('user-uuid-71'),
                    ('user-uuid-72'), ('user-uuid-73'), ('user-uuid-74'),
                    ('user-uuid-75'), ('user-uuid-76'), ('user-uuid-77'),
                    ('user-uuid-78'), ('user-uuid-79'), ('user-uuid-80')
                ) AS low_users(user_id)
            ) sub
            WHERE rn <= 30
        ) low_activity
    ) combined_activity
    LIMIT 3500
)
INSERT INTO history (
    uuid,
    user_id,
    novel_id,
    chapter_id,
    create_time,
    update_time
)
SELECT
    gen_random_uuid(),
    ua.user_id::UUID,
    ua.novel_id,
    ua.chapter_id,
    -- Create realistic timestamps spread over 60 days
    CURRENT_TIMESTAMP - INTERVAL '60 days' + (random() * INTERVAL '60 days'),
    CURRENT_TIMESTAMP - INTERVAL '1 day' + (random() * INTERVAL '1 day')
FROM user_activity ua
ORDER BY random()
LIMIT 3000;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_history_user_id ON history(user_id);
CREATE INDEX IF NOT EXISTS idx_history_novel_id ON history(novel_id);
CREATE INDEX IF NOT EXISTS idx_history_chapter_id ON history(chapter_id);
CREATE INDEX IF NOT EXISTS idx_history_create_time ON history(create_time);
CREATE INDEX IF NOT EXISTS idx_history_user_novel ON history(user_id, novel_id);
