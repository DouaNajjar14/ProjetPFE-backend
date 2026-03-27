-- Script pour insérer les spécialités universitaires
-- À exécuter dans PostgreSQL

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

SELECT 'Spécialités insérées avec succès!' AS message;
SELECT COUNT(*) as total FROM specialites_universitaires;
