-- Drop stale Hibernate CHECK constraints on enum columns so they don't block valid values
ALTER TABLE IF EXISTS candidats DROP CONSTRAINT IF EXISTS candidats_niveau_academique_check;
ALTER TABLE IF EXISTS candidatures DROP CONSTRAINT IF EXISTS candidatures_type_stage_check;
ALTER TABLE IF EXISTS candidatures DROP CONSTRAINT IF EXISTS candidatures_statut_check;
ALTER TABLE IF EXISTS pending_accounts DROP CONSTRAINT IF EXISTS pending_accounts_statut_check;
ALTER TABLE IF EXISTS pending_accounts DROP CONSTRAINT IF EXISTS pending_accounts_type_stage_check;
ALTER TABLE IF EXISTS stagiaires DROP CONSTRAINT IF EXISTS stagiaires_type_stage_check;
ALTER TABLE IF EXISTS stagiaires DROP CONSTRAINT IF EXISTS stagiaires_statut_check;

-- Force drop and recreate session_config table to fix column type issues
-- (MonthDay columns must be VARCHAR, not binary)
DROP TABLE IF EXISTS session_config CASCADE;

-- Recreate session_config table with proper VARCHAR column types
CREATE TABLE IF NOT EXISTS session_config (
    type_stage VARCHAR(50) PRIMARY KEY NOT NULL UNIQUE,
    session_type VARCHAR(50) NOT NULL,
    label VARCHAR(255) NOT NULL,
    date_debut_fixe VARCHAR(20) NOT NULL,
    date_fin_fixe VARCHAR(20),
    duree_en_mois_fixe INTEGER,
    description TEXT
);

-- Drop old columns renamed/removed by entity changes (ddl-auto=update never drops columns)
-- Old candidat_principal_id → now candidat1_id
ALTER TABLE IF EXISTS candidatures DROP COLUMN IF EXISTS candidat_principal_id;
-- Old candidat_binome_id → now candidat2_id
ALTER TABLE IF EXISTS candidatures DROP COLUMN IF EXISTS candidat_binome_id;
-- Removed dateFin field (date_debut is now used for accepted candidatures)
ALTER TABLE IF EXISTS candidatures DROP COLUMN IF EXISTS date_fin;

-- Ensure date_debut is nullable (for existing rows before acceptance)
ALTER TABLE IF EXISTS candidatures ALTER COLUMN date_debut DROP NOT NULL;

-- Ensure nullable department assignment for stagiaires
ALTER TABLE IF EXISTS stagiaires ALTER COLUMN departement_id DROP NOT NULL;

-- Remove stale legacy string column if it still exists (the entity now uses departement_id FK)
ALTER TABLE IF EXISTS stagiaires DROP COLUMN IF EXISTS departement;

-- Drop the erroneous specialite_id column from encadrants
-- (Specialites are now linked via ManyToMany join table encadrant_specialites)
ALTER TABLE IF EXISTS encadrants DROP COLUMN IF EXISTS specialite_id;

-- Drop the erroneous competences_requises column from sujet_pfe
-- (Competences are now linked via ManyToMany join table sujet_pfe_competences)
ALTER TABLE IF EXISTS sujet_pfe DROP COLUMN IF EXISTS competences_requises;

-- Drop the erroneous specialite column from sujet_pfe
-- (Specialites are now linked via ManyToMany join table sujet_pfe_specialites_universitaires)
ALTER TABLE IF EXISTS sujet_pfe DROP COLUMN IF EXISTS specialite;

-- Add nullable columns for document storage in candidats table
ALTER TABLE IF EXISTS candidats ADD COLUMN IF NOT EXISTS cv VARCHAR(500);
ALTER TABLE IF EXISTS candidats ADD COLUMN IF NOT EXISTS lettre_motivation VARCHAR(500);

-- Remove NOT NULL constraints from document columns if they exist
ALTER TABLE IF EXISTS candidats ALTER COLUMN cv DROP NOT NULL;
ALTER TABLE IF EXISTS candidats ALTER COLUMN lettre_motivation DROP NOT NULL;

-- Insert session configurations
-- Each type of stage has exactly one configuration defining the session window
-- MonthDay values are stored in ISO-8601 format: --MM-DD
INSERT INTO session_config (type_stage, session_type, label, date_debut_fixe, date_fin_fixe, duree_en_mois_fixe, description) VALUES
    ('INITIATION', 'HIVER', 'Session Hiver - Initiation', '--01-07', '--02-07', 1, 'Période de stage d''initiation obligatoire'),
    ('PERFECTIONNEMENT', 'HIVER', 'Session Hiver - Perfectionnement', '--01-07', '--02-07', 1, 'Période de stage de perfectionnement'),
    ('PFE', 'PFE', 'Session PFE', '--02-01', NULL, NULL, 'Période de projet de fin d''études - durée variable'),
    ('ETE', 'ETE', 'Session Été', '--07-01', '--08-31', 2, 'Période de stage d''été')
ON CONFLICT (type_stage) DO NOTHING;

-- Insert specialites universitaires
INSERT INTO specialites_universitaires (nom) VALUES
    ('Informatique'),
    ('Génie Logiciel'),
    ('Réseaux et Télécommunications'),
    ('Bases de Données'),
    ('Intelligence Artificielle'),
    ('Cybersécurité'),
    ('Cloud Computing'),
    ('Développement Web'),
    ('Développement Mobile'),
    ('Système d''Information'),
    ('Big Data'),
    ('IoT - Internet des Objets'),
    ('DevOps'),
    ('Architecture Microservices'),
    ('Génie Civil'),
    ('Électrotechnique'),
    ('Mécanique'),
    ('Chimie Industrielle'),
    ('Génie Chimique'),
    ('Génie Mécanique')
ON CONFLICT (nom) DO NOTHING;

