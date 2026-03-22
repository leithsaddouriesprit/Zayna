-- Migration: school-scoped filtering for agent flow
-- Run this on database zaynaa before using agent bus/candidature scoping.
-- bus.id_ecole -> ecole.id
-- candidature.id_ecole -> ecole.id

-- 1. Add id_ecole to bus (nullable for existing rows)
ALTER TABLE bus ADD COLUMN id_ecole INT NULL;
ALTER TABLE bus ADD CONSTRAINT fk_bus_ecole FOREIGN KEY (id_ecole) REFERENCES ecole(id) ON DELETE SET NULL;

-- 2. Add id_ecole to candidature (nullable for existing rows; new rows from chauffeur form will set it)
ALTER TABLE candidature ADD COLUMN id_ecole INT NULL;
ALTER TABLE candidature ADD CONSTRAINT fk_candidature_ecole FOREIGN KEY (id_ecole) REFERENCES ecole(id) ON DELETE SET NULL;
