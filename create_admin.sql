-- Script pour créer un administrateur
-- À exécuter dans PostgreSQL
-- Mot de passe: Admin123@

DO $$
DECLARE
    admin_id UUID;
BEGIN
    -- Insérer dans la table utilisateurs (héritée par administrateurs)
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, tel, role, actif, premier_login, dtype, date_creation, date_modification)
    VALUES (
        'System',
        'Admin',
        'admin@ooredoo.tn',
        '$2a$10$slYQmyNdGzin7olVN3p5Be9DlH.PKZbv5H8KnzzVgXXbVxzy990rm',
        '00000000',
        'ADMIN',
        true,
        true,
        'Admin',
        NOW(),
        NOW()
    )
    ON CONFLICT (email) DO UPDATE SET date_modification = NOW()
    RETURNING id INTO admin_id;

    -- Insérer dans la table administrateurs (table enfant, héritage JOINED)
    INSERT INTO administrateurs (id)
    VALUES (admin_id)
    ON CONFLICT (id) DO NOTHING;

    RAISE NOTICE 'Admin créé avec succès! ID: %', admin_id;
END $$;
