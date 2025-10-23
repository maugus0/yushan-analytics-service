-- Create all tables for Analytics Service
-- This migration creates comment and review tables with all necessary indexes and constraints

CREATE TABLE history (
     id SERIAL PRIMARY KEY,
     uuid UUID NOT NULL,
     user_id UUID NOT NULL,
     novel_id INTEGER NOT NULL,
     chapter_id INTEGER NOT NULL,
     create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);