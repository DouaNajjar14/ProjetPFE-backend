-- Drop stale Hibernate CHECK constraints on enum columns so they don't block valid values
ALTER TABLE IF EXISTS candidats DROP CONSTRAINT IF EXISTS candidats_niveau_academique_check;
ALTER TABLE IF EXISTS candidatures DROP CONSTRAINT IF EXISTS candidatures_type_stage_check;
ALTER TABLE IF EXISTS candidatures DROP CONSTRAINT IF EXISTS candidatures_statut_check;

-- Ensure soft-archive columns are consistent for legacy rows
ALTER TABLE IF EXISTS specialites ADD COLUMN IF NOT EXISTS archive boolean;
UPDATE specialites SET archive = false WHERE archive IS NULL;
ALTER TABLE IF EXISTS specialites ALTER COLUMN archive SET DEFAULT false;
ALTER TABLE IF EXISTS specialites ALTER COLUMN archive SET NOT NULL;

ALTER TABLE IF EXISTS competences ADD COLUMN IF NOT EXISTS archive boolean;
UPDATE competences SET archive = false WHERE archive IS NULL;
ALTER TABLE IF EXISTS competences ALTER COLUMN archive SET DEFAULT false;
ALTER TABLE IF EXISTS competences ALTER COLUMN archive SET NOT NULL;

-- Drop old columns renamed/removed by entity changes (ddl-auto=update never drops columns)
-- Old candidat_principal_id → now candidat1_id
ALTER TABLE IF EXISTS candidatures DROP COLUMN IF EXISTS candidat_principal_id;
-- Old candidat_binome_id → now candidat2_id

-- ==========================================
-- SEED DATA
-- ==========================================

-- 1. Departements
INSERT INTO departements (id, nom, archive, nombre_encadrants_actuel, nombre_stagiaires_actuel)
VALUES
('d1000000-0000-0000-0000-000000000001', 'IT', false, 0, 0),
('d1000000-0000-0000-0000-000000000002', 'RH', false, 0, 0)
ON CONFLICT (id) DO NOTHING;

-- 2. Specialites
INSERT INTO specialites (id, nom, departement_id, archive)
VALUES
(1, 'Développement Web', 'd1000000-0000-0000-0000-000000000001', false),
(2, 'Systèmes et Réseaux', 'd1000000-0000-0000-0000-000000000001', false),
(3, 'Ressources Humaines', 'd1000000-0000-0000-0000-000000000002', false)
ON CONFLICT (id) DO NOTHING;

-- 3. Competences
INSERT INTO competences (id, nom, specialite_id, archive)
VALUES
(1, 'Java', 1, false),
(2, 'Spring Boot', 1, false),
(3, 'Angular', 1, false),
(4, 'Linux', 2, false),
(5, 'Recrutement', 3, false)
ON CONFLICT (id) DO NOTHING;

-- 4. Specialite Universitaire
INSERT INTO specialites_universitaires (id, nom)
VALUES
(1, 'Génie Logiciel'),
(2, 'Informatique de Gestion')
ON CONFLICT (id) DO NOTHING;

-- 5. Universites
INSERT INTO universites (id, nom)
VALUES
('a1000000-0000-0000-0000-000000000001', 'ESPRIT'),
('a1000000-0000-0000-0000-000000000002', 'TEK-UP')
ON CONFLICT (id) DO NOTHING;

-- 6. Utilisateurs (Mot de passe pour tous: 'password')
-- Hash: $2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3POALTiLO
INSERT INTO utilisateurs (id, nom, prenom, email, mot_de_passe, role, actif, date_creation, date_modification, tel)
VALUES
('a1000000-0000-0000-0000-000000000002', 'Responsable', 'RH', 'rh@ooredoo.tn', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3POALTiLO', 'AGENT_RH', true, NOW(), NOW(), '22222222'),
('a1000000-0000-0000-0000-000000000003', 'Encadrant', 'IT', 'encadrant.it@ooredoo.tn', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3POALTiLO', 'ENCADRANT', true, NOW(), NOW(), '33333333')
ON CONFLICT (email) DO NOTHING;

-- 7. Encadrants
INSERT INTO encadrants (id, departement_id, capacite_max, capacite_actuelle)
VALUES
('a1000000-0000-0000-0000-000000000003', 'd1000000-0000-0000-0000-000000000001', 3, 0)
ON CONFLICT (id) DO NOTHING;

-- Liaison Encadrant-Specialites
INSERT INTO encadrant_specialites (encadrant_id, specialite_id)
VALUES
('a1000000-0000-0000-0000-000000000003', 1),
('a1000000-0000-0000-0000-000000000003', 2)
ON CONFLICT DO NOTHING;

-- 8. Sujet PFE
INSERT INTO sujet_pfe (id, titre, mission, nombre_stagiaires, niveau_academique, statut, departement_id, duree_en_mois, archive, date_creation)
VALUES
('b1000000-0000-0000-0000-000000000001', 'Plateforme Gestion Stagiaires', 'Conception et développement d''une application Web.', 2, 'M2', 'OUVERT', 'd1000000-0000-0000-0000-000000000001', 6, false, NOW())
ON CONFLICT (id) DO NOTHING;

-- Liaison Sujet-Competences
INSERT INTO sujet_pfe_competences (sujet_pfe_id, competence_id)
VALUES
('b1000000-0000-0000-0000-000000000001', 1),
('b1000000-0000-0000-0000-000000000001', 3)
ON CONFLICT DO NOTHING;

-- 9. Candidats
INSERT INTO candidats (id, nom, prenom, email, tel, niveau_academique, cv, universite_id)
VALUES
('c1000000-0000-0000-0000-000000000001', 'Etudiant', 'Test', 'etudiant@test.com', '55555555', 'M2', 'cv_example.pdf', 'a1000000-0000-0000-0000-000000000001')
ON CONFLICT (email) DO NOTHING;

-- 10. Candidatures
INSERT INTO candidatures (id, candidat1_id, type_stage, statut, est_binome, date_depot)
VALUES
('ca100000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'PFE', 'EN_ATTENTE', false, NOW())
ON CONFLICT (id) DO NOTHING;
ALTER TABLE IF EXISTS candidatures DROP COLUMN IF EXISTS candidat_binome_id;
-- Removed dateDebut/dateFin fields
ALTER TABLE IF EXISTS candidatures DROP COLUMN IF EXISTS date_debut;
ALTER TABLE IF EXISTS candidatures DROP COLUMN IF EXISTS date_fin;

-- Remove deprecated columns
ALTER TABLE IF EXISTS candidatures DROP COLUMN IF EXISTS date_debut;
ALTER TABLE IF EXISTS candidatures DROP COLUMN IF EXISTS date_fin;

