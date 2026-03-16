-- Drop stale Hibernate CHECK constraints on enum columns so they don't block valid values
ALTER TABLE IF EXISTS candidats DROP CONSTRAINT IF EXISTS candidats_niveau_academique_check;
ALTER TABLE IF EXISTS candidatures DROP CONSTRAINT IF EXISTS candidatures_type_stage_check;
ALTER TABLE IF EXISTS candidatures DROP CONSTRAINT IF EXISTS candidatures_statut_check;
ALTER TABLE IF EXISTS pending_accounts DROP CONSTRAINT IF EXISTS pending_accounts_statut_check;
ALTER TABLE IF EXISTS pending_accounts DROP CONSTRAINT IF EXISTS pending_accounts_type_stage_check;
ALTER TABLE IF EXISTS stagiaires DROP CONSTRAINT IF EXISTS stagiaires_type_stage_check;
ALTER TABLE IF EXISTS stagiaires DROP CONSTRAINT IF EXISTS stagiaires_statut_check;

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
