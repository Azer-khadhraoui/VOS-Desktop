# 🏢 VOS DESKTOP - Système de Gestion RH Intelligent

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-21-007396?style=for-the-badge&logo=javafx)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-00758F?style=for-the-badge&logo=mysql)
![OpenCV](https://img.shields.io/badge/OpenCV-4.12-5C3EE8?style=for-the-badge&logo=opencv)
![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36?style=for-the-badge&logo=apache-maven)

**🚀 Plateforme Complète de Management RH avec Reconnaissance Faciale & IA**

[Features](#-fonctionnalités-principales) • [Installation](#-installation) • [Démarrage Rapide](#-démarrage-rapide) • [Architecture](#-architecture) • [Documentation](#-documentation)

</div>

---

## 📋 À propos

**VOS DESKTOP** est une application desktop complète pour la gestion des ressources humaines, couvrant les processus de recrutement, évaluation, gestion des entretiens et suivi des employés. Cette plateforme utilise des technologies modernes incluant **la reconnaissance faciale**, **l'IA générative** et **JavaFX** pour une interface utilisateur fluide et intuitive.

---

## ✨ Fonctionnalités Principales

### 👥 **Gestion des Utilisateurs**
- 📱 Authentification sécurisée avec hashage BCrypt
- 👤 Gestion complète des profils utilisateur
- 🔐 Système de réinitialisation de mot de passe
- 📸 **Reconnaissance faciale** pour l'identification
- 🎨 Interface moderne et responsive

### 💼 **Gestion du Recrutement**
- 📢 Publication et gestion des offres d'emploi
- 📋 Gestion complète des candidatures
- 🤖 Analyse IA automatisée des candidats
- 📊 Tableau de bord de recrutement
- 📈 Statistiques détaillées

### 📋 **Gestion des Entretiens**
- 📅 Planification et suivi des entretiens
- 📝 Formulaires d'évaluation structurés
- ⭐ Grille d'évaluation détaillée et customizable
- 🤖 Rapport IA automatisé avec insights
- 📊 Historique complet des entretiens

### 🏢 **Gestion Administrative**
- 📋 Demandes de congés
- 📗 Gestion des démissions
- 📊 Rapports de qualité détaillés
- 📈 Statistiques globales en temps réel
- 👨‍💼 Gestion complète des droits d'accès

### 🤖 **ChatBot IA Intelligent**
- 💬 Assistant RH disponible 24/7
- 🧠 Powered by Groq API (Ultra-rapide, Gratuit)
- 📚 Réponses contextuelles et pertinentes
- 🎯 Support multi-domaines

### 🎯 **Fonctionnalités Avancées**
- 📷 **Reconnaissance Faciale** avec OpenCV
- 📹 Intégration webcam en temps réel
- 📧 Notifications par email automatisées
- 📄 Génération de rapports PDF
- 🔍 Recherche et filtrage avancés
- 🎨 Thèmes Dark/Light customisables

---

## 🛠️ Stack Technologique

| Catégorie | Technologie |
|-----------|-------------|
| **Langage** | Java 17+ |
| **Interface** | JavaFX 21 |
| **Base de Données** | MySQL 8.0+ |
| **Vision par Ordinateur** | OpenCV 4.12 |
| **Gestion Dépendances** | Maven 3.6+ |
| **IA/NLP** | Groq API (llama-3.3-70b) |
| **Sécurité** | Spring Security BCrypt |
| **PDF** | iText 7.2.5 |
| **Email** | JavaMail API 1.6.2 |
| **JSON** | Gson 2.10.1 |

---

## 📁 Structure du Projet

```
VOS-Desktop/
│
├── 📦 gestionutilisateur/                 # Module Principal
│   ├── src/main/java/
│   │   ├── controllers/                  # 30+ Contrôleurs JavaFX
│   │   │   ├── SigninController.java
│   │   │   ├── AdminViewController.java
│   │   │   ├── ChatBotController.java
│   │   │   ├── EntretienFormController.java
│   │   │   ├── WebcamController.java
│   │   │   └── ...
│   │   │
│   │   ├── entities/                     # Modèles de données (JPA)
│   │   │   ├── Utilisateur.java
│   │   │   ├── OffreEmploi.java
│   │   │   ├── Candidature.java
│   │   │   ├── Entretien.java
│   │   │   └── EvaluationEntretien.java
│   │   │
│   │   ├── services/                     # Logique métier
│   │   │   ├── AIService.java           # Groq API Integration
│   │   │   ├── ChatBotService.java      # Service IA
│   │   │   ├── EntretienService.java    # Gestion entretiens
│   │   │   ├── UtilisateurService.java  # Gestion utilisateurs
│   │   │   └── ...
│   │   │
│   │   └── utilis/                       # Utilitaires
│   │       ├── MyConnection.java        # Connexion BD
│   │       └── ...
│   │
│   ├── src/main/resources/
│   │   ├── *.fxml                       # 10+ Templates d'interface
│   │   │   ├── SigninView.fxml
│   │   │   ├── AdminOffresView.fxml
│   │   │   ├── GestionEntretienView.fxml
│   │   │   ├── ChatBotView.fxml
│   │   │   └── ...
│   │   │
│   │   ├── css/                         # Feuilles de style modernes
│   │   │   ├── modernuser.css
│   │   │   ├── entretien-modern.css
│   │   │   ├── eval-dialog.css
│   │   │   └── ...
│   │   │
│   │   ├── images/                      # Ressources graphiques
│   │   └── mail.properties              # Configuration email
│   │
│   ├── lib/opencv/                      # OpenCV 4.12 - Reconnaissance Faciale
│   │   ├── build/java/opencv-4120.jar
│   │   └── sources/                     # Sources OpenCV
│   │
│   ├── pom.xml                          # Configuration Maven complète
│   └── tokens/                          # Stockage credentials (git-ignored)
│
├── 📦 Gestion-Entretien/                 # Module Entretiens additionnels
│   └── target/classes/
│
├── 📦 gestionRecrutement/                # Module Recrutement
│
├── README.md                            # Documentation principale
├── .gitignore                          # Exclusions Git
└── LICENSE                             # Licence MIT

```

---

## 🚀 Installation Complète

### Prérequis

- **JDK 17+** : [Télécharger](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- **Maven 3.6+** : [Télécharger](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** : [Télécharger](https://dev.mysql.com/downloads/mysql/)
- **Git** : [Télécharger](https://git-scm.com/)

### Étapes d'Installation

#### 1️⃣ Cloner le Repository

```bash
git clone https://github.com/Azer-khadhraoui/VOS-Desktop.git
cd VOS-Desktop
```

#### 2️⃣ Configurer la Base de Données

```bash
# Option A : Via MySQL CLI
mysql -u root -p < database-schema.sql

# Option B : Via PhpMyAdmin
# Importer le fichier database-schema.sql dans votre interface phpMyAdmin
```

#### 3️⃣ Configurer les Variables d'Environnement

**Windows (PowerShell) :**
```powershell
$env:DB_HOST = "localhost"
$env:DB_USER = "root"
$env:DB_PASSWORD = "votre_mot_de_passe"
$env:DB_NAME = "vos_desktop"
$env:GROQ_API_KEY = "gsk_votre_cle_groq"
$env:GMAIL_APP_PASSWORD = "votre_mot_de_passe_app_gmail"
```

**Linux/Mac (Bash) :**
```bash
export DB_HOST="localhost"
export DB_USER="root"
export DB_PASSWORD="votre_mot_de_passe"
export DB_NAME="vos_desktop"
export GROQ_API_KEY="gsk_votre_cle_groq"
export GMAIL_APP_PASSWORD="votre_mot_de_passe_app_gmail"
```

#### 4️⃣ Recharger Maven

```bash
cd gestionutilisateur
mvn clean install
```

#### 5️⃣ Configurer OpenCV (Reconnaissance Faciale)

**Automatiquement (déjà configuré dans pom.xml) :**
```bash
# Le chemin est déjà défini :
# ./lib/opencv/build/java/opencv-4120.jar
```

**Si besoin de réinstaller OpenCV :**
```bash
# Linux/Mac
mvn -DopencvVersion=4.12.0 org.openpnp:opencv-maven-plugin:LATEST:setup
```

---

## 🎯 Démarrage Rapide

### ▶️ Option 1 : Depuis IntelliJ IDEA

1. Ouvrir le projet : `gestionutilisateur/`
2. **Maven** (panneau de droite) → **Reload Project**
3. Naviguer vers : `src/main/java/MainFX.java`
4. Cliquer sur **Run** ▶️

### ▶️ Option 2 : Depuis la Ligne de Commande

```bash
cd gestionutilisateur
mvn javafx:run
```

### ▶️ Option 3 : Créer un JAR Exécutable

```bash
cd gestionutilisateur
mvn clean package -DskipTests
java -jar target/vos-desktop-1.0-SNAPSHOT.jar
```

---

## 👥 Utilisateurs de Test

| Email | Mot de passe | Rôle | Permissions |
|-------|-------------|------|-------------|
| admin@vos.com | Admin123! | Administrateur | Accès complet |
| rh@vos.com | RH123! | Gestionnaire RH | Recrutement, Entretiens |
| user@vos.com | User123! | Employé | Profil, Demandes |

> ⚠️ **Important** : Modifier ces identifiants en production !

---

## 📖 Modules Détaillés

### 🔐 **Module Authentification**
- Connexion sécurisée email/mot de passe
- Réinitialisation de mot de passe par email
- Reconnaissance faciale optionnelle
- Gestion de session persistante
- Protection contre brute force

### 📊 **Module Dashboard**
- Vue d'ensemble des statistiques
- Graphiques en temps réel
- KPIs et indicateurs clés
- Alertes et notifications
- Rapports exportables

### 💼 **Module Recrutement Avancé**
- Recherche avancée avec filtres multiples
- Pipeline de candidatures visualisé
- Communication automatisée par email
- Notation et commentaires structurés
- Analyse de fit culturel avec IA

### 📋 **Module Gestion Entretiens**
- Planification de calendrier
- Rappels automatiques
- Questionnaires dynamiques
- Notation multi-critères
- Compte-rendus générés par IA

### 🎓 **Module Évaluation Performance**
- Grilles d'évaluation customisables
- Notation 360 degrés
- Rapports constructifs
- Historique complet
- Analytics par compétence

### 👨‍💼 **Module Administration**
- Gestion complète des utilisateurs
- Configuration système avancée
- Gestion des permissions par rôle
- Audit trail complet
- Sauvegarde et restauration

### 🤖 **Module ChatBot IA**
- Conversation naturelle en langage
- Compréhension contextuelle
- Support multilingue
- Réponses intelligentes
- Apprentissage continu

---

## 🔐 Fonctionnalités de Sécurité

| Fonctionnalité | Description | Status |
|---|---|---|
| 🔒 **Hashage Mots de Passe** | BCrypt + Spring Security | ✅ Implémenté |
| 🌍 **Variables d'Environnement** | Secrets externalisés | ✅ Implémenté |
| 🛡️ **Protection CSRF** | Tokens de session | ✅ Implémenté |
| 🔐 **Chiffrement SSL/TLS** | Données en transit | ✅ Disponible |
| 📝 **Audit Logs** | Traçabilité complète | ✅ Implémenté |
| 👤 **RBAC** | Contrôle d'accès granulaire | ✅ Implémenté |
| 🚫 **Rate Limiting** | Protection brute force | ✅ Implémenté |
| 📧 **2FA (Email)** | Authentification à 2 facteurs | ✅ Disponible |

---

## 📚 Documentation Complète

- **📖 Guide de Reconnaissance Faciale** : [RECONNAISSANCE_FACIALE_README.md](gestionutilisateur/RECONNAISSANCE_FACIALE_README.md)
- **📧 Configuration Email & SMTP** : [EMAIL_SETUP.md](gestionutilisateur/EMAIL_SETUP.md)
- **🔌 API Instructions** : [API_INSTRUCTIONS.md](gestionutilisateur/API_INSTRUCTIONS.md)
- **🚀 Guide Mot de Passe Oublié** : [FORGOT_PASSWORD_GUIDE.md](gestionutilisateur/FORGOT_PASSWORD_GUIDE.md)
- **📚 OpenCV Documentation** : [lib/opencv/README.md.txt](gestionutilisateur/lib/opencv/README.md.txt)
- **🤖 Groq API Docs** : https://console.groq.com/docs

---

## 🐛 Troubleshooting & Solutions

### ❌ Erreur : "Cannot find opencv-4120.jar"

```bash
# Windows
cd gestionutilisateur
mvn clean install

# Vérifier le chemin
Test-Path ".\lib\opencv\build\java\opencv-4120.jar"
```

### ❌ Problème de Connexion Base de Données

```bash
# Vérifier MySQL est lancé
mysql -h localhost -u root -p -e "SHOW DATABASES;"

# Redémarrer le service
# Windows
net start MySQL80

# Linux
sudo systemctl restart mysql
```

### ❌ Clé API Groq Manquante

```bash
# Ajouter la variable
set GROQ_API_KEY=gsk_votre_cle_ici

# Ou créer fichier .env à la racine
echo GROQ_API_KEY=gsk_votre_cle_ici > .env
```

### ❌ Problèmes de Reconnaissance Faciale

1. **Vérifier OpenCV :**
   ```bash
   mvn dependency:tree | grep opencv
   ```

2. **Tester avec exemple :**
   ```java
   System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
   System.out.println("OpenCV loaded: " + Core.VERSION);
   ```

3. **Consulter le guide :**
   - [RECONNAISSANCE_FACIALE_README.md](gestionutilisateur/RECONNAISSANCE_FACIALE_README.md)

---

## 💻 Configuration IDE Recommandée

### IntelliJ IDEA

```
File → Settings → Build Tools → Maven
✓ Importer Maven projects automatically
✓ Runner : Use Gradle
✓ Importing : Directory based structure
✓ Java 17+
✓ JavaFX SDK plugin installé

File → Project Structure
✓ Project SDK : 17+
✓ Language level : 17+
```

### VS Code

```
Extensions recommandées :
✓ Extension Pack for Java (Microsoft)
✓ Maven for Java (Microsoft)
✓ Project Manager for Java (Microsoft)
✓ JavaFX Support (brunokaique)
```

### Eclipse IDE

```
Help → Install New Software
✓ e(fx)clipse - efxclipse.bestsource.org
```

---

## 🤝 Contribution

Les contributions sont bienvenues et appréciées !

**Processus :**

1. Fork le projet (`git fork` ou bouton GitHub)
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit vos changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

**Lignes directrices :**
- Respecter la structure du code existant
- Ajouter des commentaires pour code complexe
- Tester les changements localement
- Mettre à jour la documentation si nécessaire

---

## 📄 Licence

Ce projet est sous licence **MIT**. 

Voir le fichier [LICENSE](LICENSE) pour plus de détails.

```
MIT License

Copyright (c) 2026 Azer Khadhraoui

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

---

## 📧 Contact & Support


**Ressources**
- 🔗 Repository : https://github.com/Azer-khadhraoui/VOS-Desktop
- 📋 Issues : https://github.com/Azer-khadhraoui/VOS-Desktop/issues
- 📝 Discussions : https://github.com/Azer-khadhraoui/VOS-Desktop/discussions

---

## 🎉 Remerciements Spéciaux

- 🙏 **OpenCV** pour la reconnaissance faciale performante
- 🙏 **Groq API** pour l'API IA ultra-rapide et gratuite
- 🙏 **La communauté JavaFX** pour le support
- 🙏 **Tous les contributeurs** passés et futurs
- 🙏 **Nos utilisateurs** pour leurs retours précieux

---

<div align="center">

## ⭐ Avez-vous trouvé ce projet utile ?

**N'oubliez pas de laisser une ⭐ sur GitHub !**

---

<sub>Fait avec ❤️ par VOS Team</sub>

<sub>Dernière mise à jour : Mars 2026</sub>

</div>
