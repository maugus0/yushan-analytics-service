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
