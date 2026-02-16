# VOS-Desktop

Application de gestion des utilisateurs avec JavaFX 17

## 📋 Fonctionnalités

### 🔐 Authentification
- Connexion sécurisée avec validation
- Inscription avec upload de photo de profil
- Session utilisateur persistante
- Déconnexion automatique lors de la suppression de compte

### 👤 Gestion de Profil (Client)
- **Affichage du profil** : Photo de profil, nom, prénom, email, rôle, statistiques
- **Modification** : Édition des informations personnelles avec validation en temps réel
- **Upload photo** : Sauvegarde automatique dans la base de données (chemin absolu)
- **Suppression compte** : Double confirmation avec déconnexion automatique
- **Validation des données** :
  - Noms et prénoms : lettres uniquement, min 2 caractères
  - Email : format valide
  - Mot de passe : min 6 caractères
- **Design moderne** : Dégradés violet/mauve (#667EEA→#764BA2), bordure dorée (#FFD700)

### 📊 Dashboard Administration
#### Gestion des Utilisateurs
- **Table interactive** avec toutes les informations
- **Recherche en temps réel** : Par nom, prénom, email ou rôle
- **Filtres avancés** :
  - Filtre par rôle (CLIENT, ADMIN_RH, ADMIN_TECHNIQUE)
  - Bouton de réinitialisation des filtres
  - Compteur d'utilisateurs filtrés
- **Actions CRUD** : Ajouter, modifier, supprimer avec modals animés
- **Upload de photos** lors de l'ajout/modification

#### Page Statistiques
- **KPI Cards avec couleurs vives et visibles** :
  - Total utilisateurs (violet vif #5D3FD3→#3F2CAF)
  - Clients (rose vif #EC1C8C→#D80032)
  - Admins (cyan vif #00B4D8→#0088CC)
  - Récents - 10 derniers inscrits (jaune vif #FFD60A→#FFA500)
- **Graphiques interactifs avec données réelles** :
  - LineChart : Inscriptions réparties par jour (projection réelle sur 7 jours)
  - PieChart : Répartition par rôle (CLIENT, ADMIN_RH, ADMIN_TECHNIQUE)
- **Flux d'activité récente** : Top 5 utilisateurs les plus récents (ID max) avec animations
- **Statistiques réelles** :
  - Croissance calculée : Comparaison des 10 derniers IDs vs 10 précédents
  - Comptage exact par rôle depuis la base de données
  - Distribution réelle dans les graphiques
- **Export CSV** : Exportation des statistiques et détails utilisateurs
- **Auto-refresh** : Actualisation automatique toutes les 30 secondes
- **Bouton d'actualisation manuelle**

### 🎨 Design
- **Interface moderne** avec dégradés et ombres portées
- **Animations fluides** : FadeTransition, ScaleTransition, TranslateTransition
- **Sidebar avec hover** : Expansion au survol avec labels animés
- **Thème cohérent** : Bleu/violet avec accents dorés et verts
- **Responsive** : 1440x1024px

### 🔧 Fonctionnalités Innovantes
1. **Statistiques 100% réelles** : Toutes les données proviennent directement de la base de données
2. **Graphique inscriptions/jour** : Distribution réelle des utilisateurs sur 7 jours théoriques
3. **Calcul de croissance intelligent** : Comparaison des 10 derniers IDs vs 10 précédents pour mesurer la tendance
4. **Auto-refresh des statistiques** : Mise à jour toutes les 30 secondes avec arrêt automatique lors du changement de vue
5. **Couleurs vives et visibles** : KPI cards avec gradients vibrants pour meilleure lisibilité
6. **Filtrage combiné admin** : Recherche textuelle + filtre de rôle simultanés
7. **Compteur dynamique** : Nombre d'utilisateurs affichés selon les filtres
8. **Animations d'apparition** : FadeTransition sur les éléments d'activité
9. **Validation en temps réel** : Feedback immédiat sur les champs de formulaire
10. **Export de données** : CSV avec horodatage automatique
11. **Avatars personnalisés** : Upload d'images avec prévisualisation circulaire
12. **Double confirmation** : Suppression de compte avec saisie d'email de confirmation
13. **Logs détaillés** : Console avec symboles ✓/✗/ℹ/⚠ pour le débogage
14. **Top 5 récents** : Activité affichant les utilisateurs avec les IDs les plus élevés

## 🏗️ Architecture

### Structure MVC
- **Models** : `entities/Utilisateur.java`
- **Views** : FXML (SigninView, SignupView, ProfilView, OffresView, AdministrationView, StatistiquesView)
- **Controllers** : Package `controllers/`
- **Services** : `services/ServiceUtilisateur.java`
- **Utils** : `utilis/MyConnection.java`, `utilis/UserSession.java`

### Base de Données
- **Connexion** : MySQL via MyConnection
- **Champs Utilisateur** :
  - id_utilisateur (INT AUTO_INCREMENT)
  - image_profil (VARCHAR) - Chemin absolu
  - email (VARCHAR UNIQUE)
  - mot_de_passe (VARCHAR)
  - role (ENUM: CLIENT, ADMIN_RH, ADMIN_TECHNIQUE)
  - nom (VARCHAR)
  - prenom (VARCHAR)

## 🐛 Corrections Appliquées

### Bug Critique : Images de Profil
**Problème** : Images uploadées lors de l'inscription ne se chargeaient pas (seul le nom de fichier était sauvegardé)
**Solution** : Ajout de `imageAbsolutePath` dans SignupController, sauvegarde du chemin absolu complet

### Validation des Données
**Problème** : Aucune validation, données invalides possibles
**Solution** : Validation en temps réel + validation à la sauvegarde avec messages d'erreur détaillés

### Auto-logout
**Problème** : Utilisateur restait connecté après suppression de compte
**Solution** : `UserSession.setCurrentUser(null)` + redirection vers SigninView

## 🚀 Technologies
- **JavaFX 17** : Framework UI
- **FXML** : Markup pour les interfaces
- **CSS** : Styles personnalisés (hex uniquement, pas de hsl())
- **MySQL** : Base de données
- **Maven** : Gestion des dépendances

## 📦 Installation

1. Cloner le repository
2. Configurer MySQL et créer la base de données
3. Mettre à jour `MyConnection.java` avec vos identifiants
4. Compiler avec Maven : `mvn clean install`
5. Lancer : `mvn javafx:run` ou exécuter `MainFX.java`

## 👨‍💻 Développeur
Mohamed Azer Khadhraoui

---
✨ **Version actuelle** : 2.0 avec Dashboard Admin et Statistiques
