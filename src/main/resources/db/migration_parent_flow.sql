-- Migration: parent flow (users + parent, parent.user_id -> users.id)
-- Run on database zaynaa for parent registration and login.
-- No enfant created at account creation. candidature_enfant.id_ecole already used by Leith.
-- If a column or constraint already exists, skip the corresponding line.

-- 1. parent table: link to users
ALTER TABLE parent ADD COLUMN user_id INT NULL;
ALTER TABLE parent ADD CONSTRAINT fk_parent_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 2. parent.prenom (for Nom/Prénom split)
ALTER TABLE parent ADD COLUMN prenom VARCHAR(255) NULL;
