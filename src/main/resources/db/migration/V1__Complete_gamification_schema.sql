-- Gamification Service Database Schema and Initial Data
-- Combined migration file for all gamification tables and data

-- ========================================
-- TABLE CREATIONS
-- ========================================

CREATE TABLE IF NOT EXISTS exp_transactions (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS yuan_transactions (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS daily_reward_log (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    last_reward_date DATE NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS achievements (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    criteria_json JSONB,
    icon_url TEXT
);

CREATE TABLE IF NOT EXISTS user_achievements (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    achievement_id VARCHAR(100) NOT NULL REFERENCES achievements(id),
    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, achievement_id)
);

-- ========================================
-- INDEXES
-- ========================================

CREATE INDEX IF NOT EXISTS idx_exp_transactions_user_id ON exp_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_yuan_transactions_user_id ON yuan_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_achievements_user_id ON user_achievements(user_id);

-- ========================================
-- INITIAL ACHIEVEMENTS DATA
-- ========================================

-- Login Achievements
INSERT INTO achievements (id, name, description, criteria_json, icon_url) VALUES
('WELCOME_TO_YUSHAN', 'Welcome to Yushan', 'Welcome to the Yushan platform! Complete your first login to unlock this achievement.', '{"type": "login", "count": 1}', 'https://example.com/icons/welcome.png');

-- Comment Achievements
INSERT INTO achievements (id, name, description, criteria_json, icon_url) VALUES
('FIRST_CRY', 'First Comment', 'Share your first thoughts by posting a comment.', '{"type": "comment", "count": 1}', 'https://example.com/icons/first-comment.png'),
('ELOQUENT_SPEAKER', 'Comment Enthusiast', 'You are an active participant! Post 10 comments.', '{"type": "comment", "count": 10}', 'https://example.com/icons/comment-enthusiast.png'),
('COMMENT_MASTER', 'Comment Master', 'You are a true comment master! Post 50 comments.', '{"type": "comment", "count": 50}', 'https://example.com/icons/comment-master.png');

-- Review Achievements
INSERT INTO achievements (id, name, description, criteria_json, icon_url) VALUES
('REVIEW_ROOKIE', 'First Review', 'Share your first review to help others make informed decisions.', '{"type": "review", "count": 1}', 'https://example.com/icons/first-review.png'),
('INSIGHTFUL_CRITIC', 'Insightful Critic', 'Share your insights by posting 10 reviews.', '{"type": "review", "count": 10}', 'https://example.com/icons/insightful-critic.png'),
('LITERARY_GURU', 'Literary Guru', 'Become a master of critique by posting 50 reviews.', '{"type": "review", "count": 50}', 'https://example.com/icons/literary-guru.png');

-- Vote Achievements
INSERT INTO achievements (id, name, description, criteria_json, icon_url) VALUES
('SHARP_EYE', 'Sharp Eye', 'Vote for a novel for the first time.', '{"type": "vote", "count": 1}', 'https://example.com/icons/sharp-eye.png'),
('TASTE_MAKER', 'Taste Maker', 'Your voice matters! Cast 10 votes.', '{"type": "vote", "count": 10}', 'https://example.com/icons/taste-maker.png');

-- Level Achievements
INSERT INTO achievements (id, name, description, criteria_json, icon_url) VALUES
('GETTING_GOOD', 'Getting Good', 'Reach Level 3.', '{"type": "level", "target": 3}', 'https://example.com/icons/getting-good.png'),
('ACCOMPLISHED_SCHOLAR', 'Accomplished Scholar', 'Reach Level 5.', '{"type": "level", "target": 5}', 'https://example.com/icons/accomplished-scholar.png');
