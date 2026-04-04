-- Réclamations niveau 3 : assignation + historique (migration additive)
-- À exécuter sur la base Zayna (MySQL) après sauvegarde.

-- 1) Colonnes d'assignation sur reclamation (aucun changement aux ENUM statut existants)
ALTER TABLE reclamation
    ADD COLUMN user_id_assigne INT NULL DEFAULT NULL COMMENT 'Utilisateur responsable du traitement' AFTER user_id,
    ADD COLUMN role_assigne ENUM('PARENT','CHAUFFEUR','MAITRESSE','AGENT_ECOLE','ADMIN') NULL DEFAULT NULL AFTER user_id_assigne,
    ADD COLUMN date_assignation TIMESTAMP NULL DEFAULT NULL AFTER role_assigne;

ALTER TABLE reclamation
    ADD CONSTRAINT fk_reclamation_user_assigne
        FOREIGN KEY (user_id_assigne) REFERENCES users(id) ON DELETE SET NULL;

-- 2) Historique / traçabilité
CREATE TABLE IF NOT EXISTS reclamation_historique (
    id INT NOT NULL AUTO_INCREMENT,
    reclamation_id INT NOT NULL,
    action_type VARCHAR(40) NOT NULL,
    ancien_statut VARCHAR(32) NULL,
    nouveau_statut VARCHAR(32) NULL,
    ancien_assigne INT NULL,
    nouveau_assigne INT NULL,
    message_action TEXT NULL,
    user_id_action INT NOT NULL,
    role_action VARCHAR(40) NOT NULL,
    date_action TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_rec_hist_reclamation (reclamation_id),
    KEY idx_rec_hist_date (date_action),
    CONSTRAINT fk_rec_hist_reclamation FOREIGN KEY (reclamation_id) REFERENCES reclamation(id) ON DELETE CASCADE,
    CONSTRAINT fk_rec_hist_user FOREIGN KEY (user_id_action) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
