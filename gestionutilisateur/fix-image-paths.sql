-- Script SQL pour corriger les chemins d'images dans la base de données
-- IMPORTANT: Remplace les chemins par TES vrais chemins absolus

-- 1. D'abord, vérifie les chemins actuels
SELECT 
    id_utilisateur, 
    nom, 
    prenom, 
    email, 
    image_profil,
    CASE 
        WHEN image_profil LIKE '%:\%' THEN 'Chemin absolu ✓'
        WHEN image_profil LIKE '%/%' THEN 'Chemin absolu ✓'
        ELSE 'Chemin relatif ⚠ (à corriger)'
    END AS statut
FROM utilisateur
WHERE image_profil IS NOT NULL AND image_profil != '';

-- 2. Pour corriger UN utilisateur spécifique (exemple pour l'ID 1)
-- REMPLACE LE CHEMIN PAR TON VRAI CHEMIN !
UPDATE utilisateur
SET image_profil = 'C:\Users\Azer Khadhraoui\Desktop\VOS-Desktop\gestionutilisateur\images\photo_linkedin_1771108063689.jpg'
WHERE id_utilisateur = 1;

-- Ou par email :
UPDATE utilisateur
SET image_profil = 'C:\Users\Azer Khadhraoui\Desktop\VOS-Desktop\gestionutilisateur\images\photo_linkedin_1771108063689.jpg'
WHERE email = 'mohamedazer.khadhraoui@gmail.com';

-- 3. Si tous tes fichiers sont dans le dossier "images/" et ont juste le nom de fichier,
--    tu peux faire une mise à jour en masse (ATTENTION: teste d'abord sur 1 utilisateur !)
-- UPDATE utilisateur
-- SET image_profil = CONCAT('C:\Users\Azer Khadhraoui\Desktop\VOS-Desktop\gestionutilisateur\images\', image_profil)
-- WHERE image_profil NOT LIKE '%:\%'  -- Ne met à jour que les chemins relatifs
--   AND image_profil IS NOT NULL 
--   AND image_profil != '';

-- 4. Vérification finale : tous les chemins doivent être absolus
SELECT 
    id_utilisateur, 
    nom, 
    prenom, 
    image_profil,
    CASE 
        WHEN image_profil LIKE 'C:\%' THEN '✓ OK'
        ELSE '⚠ À corriger'
    END AS verification
FROM utilisateur
WHERE image_profil IS NOT NULL AND image_profil != '';
