-- ========================================
-- SCRIPT DE CORRECTION DES CHEMINS D'IMAGES
-- ========================================
-- Copie ce script et exécute-le dans phpMyAdmin ou MySQL Workbench

-- 1. VÉRIFIER LES CHEMINS ACTUELS (AVANT CORRECTION)
SELECT id_utilisateur, nom, prenom, email, 
       CHAR_LENGTH(image_profil) as longueur_chemin,
       image_profil 
FROM utilisateur
ORDER BY id_utilisateur;

-- 2. CORRIGER LES CHEMINS DUPLIQUÉS
-- Cas 1: Chemins avec duplication "images/C:\Users\..." ou "images\C:\Users\..."
-- On extrait le nom du fichier et on reconstruit le bon chemin

-- Pour jacer (exemple: images/C:\Users\...\images\jacer_1771264636867.jpg)
UPDATE utilisateur
SET image_profil = CONCAT(
    'C:/Users/Azer Khadhraoui/Desktop/VOS-Desktop/gestionutilisateur/images/',
    SUBSTRING_INDEX(image_profil, '\\', -1)
)
WHERE image_profil LIKE '%images/C:\\Users\\%\\images\\%'
   OR image_profil LIKE '%images\\C:\\Users\\%\\images\\%';

-- Pour les chemins avec images externes (h, azer avec logo.png, etc.)
UPDATE utilisateur
SET image_profil = CONCAT(
    'C:/Users/Azer Khadhraoui/Desktop/VOS-Desktop/gestionutilisateur/images/',
    SUBSTRING_INDEX(SUBSTRING_INDEX(image_profil, '\\', -1), '/', -1)
)
WHERE (image_profil LIKE '%images/C:\\Users\\%' OR image_profil LIKE '%images\\C:\\Users\\%')
  AND image_profil NOT LIKE 'C:/Users/Azer Khadhraoui/Desktop/VOS-Desktop/gestionutilisateur/images/%';

-- 3. METTRE À NULL LES CHEMINS VERS DES FICHIERS QUI N'EXISTENT PAS
-- (Facultatif - pour forcer l'icône par défaut)
-- UPDATE utilisateur SET image_profil = NULL WHERE nom = 'fares' AND image_profil LIKE '%neymar%';
-- UPDATE utilisateur SET image_profil = NULL WHERE nom = 'aa' AND image_profil LIKE '%default.png%';

-- 4. VÉRIFIER LE RÉSULTAT (APRÈS CORRECTION)
SELECT id_utilisateur, nom, prenom, email,
       CHAR_LENGTH(image_profil) as longueur_chemin,
       image_profil 
FROM utilisateur
ORDER BY id_utilisateur;

-- 5. REQUÊTES INDIVIDUELLES (SI BESOIN)
-- Pour corriger un utilisateur spécifique (exemple: jacer)
-- UPDATE utilisateur
-- SET image_profil = 'C:/Users/Azer Khadhraoui/Desktop/VOS-Desktop/gestionutilisateur/images/jacer_1771264636867.jpg'
-- WHERE nom = 'jacer';
