-- Maitresse métier (école + bus optionnel), candidature agent école, backfill agents existants.
-- Exécuter sur la base zaynaa avant d'utiliser les nouveaux écrans.

CREATE TABLE IF NOT EXISTS maitresse (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nom VARCHAR(100) NOT NULL,
  prenom VARCHAR(100) NOT NULL,
  id_ecole INT NOT NULL,
  id_bus INT NULL,
  user_id INT NOT NULL,
  UNIQUE KEY uk_maitresse_user (user_id),
  KEY idx_maitresse_ecole (id_ecole),
  KEY idx_maitresse_bus (id_bus),
  CONSTRAINT fk_maitresse_ecole FOREIGN KEY (id_ecole) REFERENCES ecole(id),
  CONSTRAINT fk_maitresse_bus FOREIGN KEY (id_bus) REFERENCES bus(id) ON DELETE SET NULL,
  CONSTRAINT fk_maitresse_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS candidature_agent (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  nom VARCHAR(100) NOT NULL,
  prenom VARCHAR(100) NOT NULL,
  id_ecole INT NOT NULL,
  latitude DOUBLE NOT NULL,
  longitude DOUBLE NOT NULL,
  statut ENUM('EN_ATTENTE','APPROUVEE','REFUSEE') NOT NULL DEFAULT 'EN_ATTENTE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_candidature_agent_user (user_id),
  CONSTRAINT fk_candidature_agent_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_candidature_agent_ecole FOREIGN KEY (id_ecole) REFERENCES ecole(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Si la colonne manque : ALTER TABLE enfant ADD COLUMN on_board TINYINT(1) NOT NULL DEFAULT 0;

-- Agents déjà en base : candidature approuvée (siné ils seraient bloqués sans ligne).
INSERT INTO candidature_agent (user_id, nom, prenom, id_ecole, latitude, longitude, statut)
SELECT ae.user_id,
       COALESCE(NULLIF(TRIM(ae.nom), ''), u.nom, ''),
       COALESCE(NULLIF(TRIM(ae.prenom), ''), ''),
       ae.id_ecole,
       COALESCE(e.latitude, 0),
       COALESCE(e.longitude, 0),
       'APPROUVEE'
FROM agent_ecole ae
JOIN users u ON u.id = ae.user_id
JOIN ecole e ON e.id = ae.id_ecole
WHERE NOT EXISTS (SELECT 1 FROM candidature_agent c WHERE c.user_id = ae.user_id);

-- Optionnel : comptes MAITRESSE déjà dans `users` sans ligne `maitresse` (ancienne inscription Talel).
-- Adapter id_ecole (ex. 1) et la découpe nom/prénom selon vos données.
-- INSERT INTO maitresse (nom, prenom, id_ecole, id_bus, user_id)
-- SELECT TRIM(SUBSTRING_INDEX(u.nom, ' ', 1)), TRIM(SUBSTRING_INDEX(u.nom, ' ', -1)), 1, NULL, u.id
-- FROM users u WHERE u.categorie = 'MAITRESSE'
-- AND NOT EXISTS (SELECT 1 FROM maitresse m WHERE m.user_id = u.id);
