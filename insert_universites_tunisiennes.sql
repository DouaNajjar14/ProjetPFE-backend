-- ============================================================
--  Insertion des universités tunisiennes (publiques + privées)
--  Table : universites
--  Exécuter dans pgAdmin sur la base : gestion_stagiaires
-- ============================================================

INSERT INTO universites (id, nom) VALUES

-- ─────────────────────────────────────────
--  UNIVERSITÉS PUBLIQUES (étatiques)
-- ─────────────────────────────────────────

-- Université de Tunis
(gen_random_uuid(), 'Université de Tunis'),
(gen_random_uuid(), 'École Nationale Supérieure d''Ingénieurs de Tunis (ENSIT)'),
(gen_random_uuid(), 'Institut Supérieur de Gestion de Tunis (ISG Tunis)'),
(gen_random_uuid(), 'Institut Supérieur des Arts Multimédia de la Manouba (ISAMM)'),

-- Université de Tunis El Manar
(gen_random_uuid(), 'Université de Tunis El Manar'),
(gen_random_uuid(), 'Faculté des Sciences de Tunis (FST)'),
(gen_random_uuid(), 'Faculté de Médecine de Tunis'),
(gen_random_uuid(), 'École Nationale d''Ingénieurs de Tunis (ENIT)'),
(gen_random_uuid(), 'Institut Supérieur d''Informatique de Tunis (ISI)'),
(gen_random_uuid(), 'Institut Supérieur des Technologies de l''Information et de la Communication (ISTIC)'),

-- Université de Carthage
(gen_random_uuid(), 'Université de Carthage'),
(gen_random_uuid(), 'École Polytechnique de Tunisie (EPT)'),
(gen_random_uuid(), 'École Nationale des Sciences de l''Informatique (ENSI)'),
(gen_random_uuid(), 'École Supérieure de Commerce de Tunis (ESCT)'),
(gen_random_uuid(), 'Institut National des Sciences Appliquées et de Technologie (INSAT)'),
(gen_random_uuid(), 'Faculté des Sciences Économiques et de Gestion de Nabeul (FSEGN)'),

-- Université de la Manouba
(gen_random_uuid(), 'Université de la Manouba'),
(gen_random_uuid(), 'École Nationale des Sciences de l''Informatique de la Manouba (ENSI Manouba)'),
(gen_random_uuid(), 'École Supérieure de Commerce de Tunis — Manouba (ESCT Manouba)'),
(gen_random_uuid(), 'Institut Supérieur des Études Technologiques de la Manouba (ISET Manouba)'),
(gen_random_uuid(), 'Institut Supérieur des Langues de Tunis (ISLT)'),

-- Université de Sfax
(gen_random_uuid(), 'Université de Sfax'),
(gen_random_uuid(), 'École Nationale d''Ingénieurs de Sfax (ENIS)'),
(gen_random_uuid(), 'Faculté des Sciences de Sfax (FSS)'),
(gen_random_uuid(), 'Faculté de Médecine de Sfax'),
(gen_random_uuid(), 'Institut Supérieur d''Informatique et de Multimédia de Sfax (ISIMS)'),
(gen_random_uuid(), 'Institut Supérieur de Gestion de Sfax (ISG Sfax)'),

-- Université de Sousse
(gen_random_uuid(), 'Université de Sousse'),
(gen_random_uuid(), 'École Nationale d''Ingénieurs de Sousse (ENISo)'),
(gen_random_uuid(), 'Institut Supérieur d''Informatique et de Technologie de Communication de Hammam Sousse (ISITCOM)'),
(gen_random_uuid(), 'Faculté des Sciences Économiques et de Gestion de Sousse (FSEGS)'),
(gen_random_uuid(), 'Institut Supérieur de Gestion de Sousse (ISG Sousse)'),

-- Université de Monastir
(gen_random_uuid(), 'Université de Monastir'),
(gen_random_uuid(), 'École Nationale d''Ingénieurs de Monastir (ENIM)'),
(gen_random_uuid(), 'Faculté de Médecine de Monastir'),
(gen_random_uuid(), 'Faculté des Sciences de Monastir (FSM)'),
(gen_random_uuid(), 'Institut Supérieur d''Informatique de Mahdia (ISIMA)'),

-- Université de Gafsa
(gen_random_uuid(), 'Université de Gafsa'),
(gen_random_uuid(), 'Faculté des Sciences de Gafsa (FSG)'),
(gen_random_uuid(), 'Institut Supérieur des Arts et Métiers de Gafsa (ISAMG)'),

-- Université de Gabès
(gen_random_uuid(), 'Université de Gabès'),
(gen_random_uuid(), 'École Nationale d''Ingénieurs de Gabès (ENIG)'),
(gen_random_uuid(), 'Institut Supérieur d''Informatique et de Multimédia de Gabès (ISIMG)'),

-- Université de Jendouba
(gen_random_uuid(), 'Université de Jendouba'),
(gen_random_uuid(), 'Faculté des Sciences Juridiques, Économiques et de Gestion de Jendouba (FSJEGS)'),

-- Université de Kairouan
(gen_random_uuid(), 'Université de Kairouan'),
(gen_random_uuid(), 'Faculté des Sciences et Techniques de Sidi Bouzid (FSTS)'),
(gen_random_uuid(), 'Institut Supérieur d''Informatique de Kairouan (ISIK)'),

-- Université de Bizerte
(gen_random_uuid(), 'Université de Bizerte'),
(gen_random_uuid(), 'Faculté des Sciences de Bizerte (FSB)'),
(gen_random_uuid(), 'Institut Supérieur des Sciences Appliquées et de Technologie de Mateur (ISSAT Mateur)'),

-- Universités virtuelles / spécialisées publiques
(gen_random_uuid(), 'Université Virtuelle de Tunis (UVT)'),
(gen_random_uuid(), 'École Nationale d''Administration (ENA)'),
(gen_random_uuid(), 'Institut Supérieur de Comptabilité et d''Administration des Entreprises (ISCAE)'),

-- ─────────────────────────────────────────
--  UNIVERSITÉS PRIVÉES
-- ─────────────────────────────────────────

(gen_random_uuid(), 'Université Privée de Tunis (UPT)'),
(gen_random_uuid(), 'Université Centrale de Tunis (UCT)'),
(gen_random_uuid(), 'Université Libre de Tunis (ULT)'),
(gen_random_uuid(), 'Université des Sciences, des Arts et des Métiers (USAM)'),
(gen_random_uuid(), 'Université Internationale de Tunis (UIT)'),
(gen_random_uuid(), 'Université Méditerranéenne Privée de Tunis (UMPT)'),
(gen_random_uuid(), 'École Supérieure Privée d''Ingénierie et de Technologie (ESPRIT)'),
(gen_random_uuid(), 'École Supérieure Privée d''Informatique et de Management (Horizon)'),
(gen_random_uuid(), 'Institut Supérieur Privé Polytechnique de Sfax (IPPS)'),
(gen_random_uuid(), 'Université Ibn Khaldoun de Tunis (UIK)'),
(gen_random_uuid(), 'Institut Supérieur des Technologies de l''Information (ISTEAM)'),
(gen_random_uuid(), 'Higher Institute of Technological Studies (HITES)'),
(gen_random_uuid(), 'Université Tunis Carthage (UTC) — Privée'),
(gen_random_uuid(), 'École Polytechnique Privée de Sousse (EPPS)'),
(gen_random_uuid(), 'École Supérieure Privée de Commerce et de Comptabilité (ESCC)'),
(gen_random_uuid(), 'Institut des Hautes Études Commerciales (IHEC) — Privé');

