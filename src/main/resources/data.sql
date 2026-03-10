-- Drop stale Hibernate CHECK constraints on enum columns so they don't block valid values
ALTER TABLE IF EXISTS candidats DROP CONSTRAINT IF EXISTS candidats_niveau_academique_check;
ALTER TABLE IF EXISTS candidatures DROP CONSTRAINT IF EXISTS candidatures_type_stage_check;
ALTER TABLE IF EXISTS candidatures DROP CONSTRAINT IF EXISTS candidatures_statut_check;

-- Drop old columns renamed/removed by entity changes (ddl-auto=update never drops columns)
-- Old candidat_principal_id → now candidat1_id
ALTER TABLE IF EXISTS candidatures DROP COLUMN IF EXISTS candidat_principal_id;
-- Old candidat_binome_id → now candidat2_id
ALTER TABLE IF EXISTS candidatures DROP COLUMN IF EXISTS candidat_binome_id;
-- Removed dateDebut/dateFin fields
ALTER TABLE IF EXISTS candidatures DROP COLUMN IF EXISTS date_debut;
ALTER TABLE IF EXISTS candidatures DROP COLUMN IF EXISTS date_fin;

-- Remove deprecated columns
ALTER TABLE IF EXISTS candidatures DROP COLUMN IF EXISTS date_debut;
ALTER TABLE IF EXISTS candidatures DROP COLUMN IF EXISTS date_fin;
