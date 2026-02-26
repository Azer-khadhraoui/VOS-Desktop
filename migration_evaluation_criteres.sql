-- Script de migration pour ajouter les critères d'évaluation
-- À exécuter dans votre base de données MySQL

-- Ajouter les nouvelles colonnes à la table evaluation_entretien
ALTER TABLE evaluation_entretien 
ADD COLUMN competences_techniques INT DEFAULT 0,
ADD COLUMN competences_comportementales INT DEFAULT 0,
ADD COLUMN communication INT DEFAULT 0,
ADD COLUMN motivation INT DEFAULT 0,
ADD COLUMN experience INT DEFAULT 0;

-- Vérification
SELECT * FROM evaluation_entretien LIMIT 1;
