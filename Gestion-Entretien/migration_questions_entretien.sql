-- Migration : Ajout du champ questions_entretien à la table entretien
-- Date : 20/02/2026
-- Description : Permet de stocker les questions générées par IA pour chaque entretien

-- Ajouter la colonne questions_entretien (texte long)
ALTER TABLE entretien 
ADD COLUMN questions_entretien TEXT NULL 
COMMENT 'Questions d''entretien générées par IA ou saisies manuellement';

-- Vérification
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'entretien' AND COLUMN_NAME = 'questions_entretien';

COMMIT;
