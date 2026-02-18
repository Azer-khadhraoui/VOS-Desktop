# 🔐 Guide de Configuration - Mot de Passe Oublié

## ✅ Fichiers créés

1. **ForgotPasswordView.fxml** - Page d'envoi du code (email + vérification)
2. **ResetPasswordView.fxml** - Page de réinitialisation du mot de passe
3. **ForgotPasswordController.java** - Logique de gestion
4. **EmailService.java** - Service d'envoi d'emails
5. **Lien ajouté sur SigninView** - "Mot de passe oublié ?"

---

## 📸 Images de fond à ajouter

Place les deux images dans le dossier : `src/main/resources/img/`

1. **mdpbackG.png** → Image pour la première page (email + code)
2. **mdpconfirmbackG.png** → Image pour la page de réinitialisation

---

## 📧 Configuration Gmail (pour l'envoi d'emails)

### Étape 1 : Activer l'authentification à 2 facteurs

1. Va sur [myaccount.google.com](https://myaccount.google.com)
2. Clique sur **Sécurité**
3. Active **Validation en deux étapes**

### Étape 2 : Générer un mot de passe d'application

1. Toujours dans **Sécurité**, cherche **Mots de passe des applications**
2. Sélectionne **Autre (nom personnalisé)** et entre "VOS App"
3. Clique sur **Générer**
4. **Copie le mot de passe de 16 caractères** (ex: `abcd efgh ijkl mnop`)

### Étape 3 : Configurer EmailService.java

Ouvre `src/main/java/services/EmailService.java` et modifie la ligne 15 :

```java
// AVANT
private static final String SENDER_PASSWORD = ""; // À configurer

// APRÈS (remplace par ton mot de passe d'application)
private static final String SENDER_PASSWORD = "abcd efgh ijkl mnop";
```

⚠️ **IMPORTANT** : Ne partage JAMAIS ce mot de passe ! Ne le commite pas sur GitHub !

---

## 🚀 Test de la fonctionnalité

### Mode développement (sans Gmail configuré)

Si `SENDER_PASSWORD` est vide, le code s'affiche dans la console :

```
📧 Code généré pour user@example.com: 123456
⚠ MODE DÉVELOPPEMENT : Email non envoyé (configurez SENDER_PASSWORD)
✓ Code de vérification : 123456
```

### Mode production (avec Gmail configuré)

1. Clique sur **"Mot de passe oublié ?"** sur la page de connexion
2. Entre ton email : `azerronaldo2004@gmail.com`
3. Clique sur **"Envoyer le code"**
4. Vérifie ta boîte mail
5. Entre le code reçu (6 chiffres)
6. Clique sur **"Vérifier"**
7. Entre ton nouveau mot de passe
8. Confirme le mot de passe
9. Clique sur **"Réinitialiser le mot de passe"**
10. Tu seras redirigé vers la page de connexion

---

## 🔒 Sécurité

- ✅ Code valable **10 minutes** seulement
- ✅ Validation du format email
- ✅ Vérification que l'utilisateur existe
- ✅ Confirmation du mot de passe
- ✅ Minimum 6 caractères pour le mot de passe
- ✅ Code stocké en mémoire (pas dans la DB)

---

## 📝 Email HTML envoyé

L'email envoyé contient :
- Un design professionnel
- Le code en gros et centré
- Avertissement de sécurité
- Durée de validité (10 minutes)

---

## 🛠️ Commandes pour tester

```bash
# Recompiler le projet
cd gestionutilisateur
mvn clean compile

# Ou depuis IntelliJ IDEA
# Build → Rebuild Project
```

---

## ❓ Problèmes courants

### L'email n'arrive pas
- Vérifie que le mot de passe d'application est correct
- Vérifie ta connexion internet
- Regarde dans les spams

### Erreur "Authentication failed"
- Le mot de passe d'application est incorrect
- L'authentification à 2 facteurs n'est pas activée

### Code expiré
- Le code est valable 10 minutes
- Redemande un nouveau code

---

## 🎨 Personnalisation

Tu peux modifier :
- **Durée de validité du code** : ligne 15 dans `EmailService.java` (`CODE_VALIDITY_MS`)
- **Longueur du code** : ligne 23 dans `EmailService.java` (actuellement 6 chiffres)
- **Texte de l'email** : lignes 62-95 dans `EmailService.java`
- **Couleurs des pages** : dans les fichiers FXML (utilise `styleUser.css`)

---

## ✅ Prêt à utiliser !

Une fois configuré, la fonctionnalité "Mot de passe oublié" est complète et sécurisée ! 🎉
