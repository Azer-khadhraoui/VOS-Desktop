# ⚙️ Configuration de l'envoi d'emails

## � Installation rapide

### 1. Créer votre fichier EmailService.java

```bash
# Copiez le template
cd src/main/java/services
cp EmailService.java.template EmailService.java
```

Ou manuellement : **Copiez** `EmailService.java.template` → **Renommez** en `EmailService.java`

### 2. Configurer vos credentials Gmail

Ouvrez `src/main/java/services/EmailService.java` et modifiez les lignes 18-19 :

```java
private static final String SENDER_EMAIL = "votre-email@gmail.com"; // Votre email
private static final String SENDER_PASSWORD = "xxxx xxxx xxxx xxxx"; // Votre mot de passe d'application
```

## 📧 Générer un mot de passe d'application Gmail

1. **Activez l'authentification à 2 facteurs** sur votre compte Gmail
   - Allez sur [myaccount.google.com/security](https://myaccount.google.com/security)

2. **Générez un mot de passe d'application** :
   - Dans "Sécurité", cherchez "Mots de passe des applications"
   - Sélectionnez "Autre (nom personnalisé)" et entrez "VOS App"
   - Cliquez sur "Générer"
   - **Copiez le mot de passe de 16 caractères** (format: `xxxx xxxx xxxx xxxx`)

3. **Collez-le** dans `EmailService.java` ligne 19

## 🔒 Sécurité - IMPORTANT !

✅ **EmailService.java est dans .gitignore**  
✅ **Vous pouvez commiter en toute sécurité**  
✅ **Chaque développeur configure son propre fichier**  
❌ **NE COMMITEZ JAMAIS EmailService.java**  

## 💡 Mode développement

Si `SENDER_PASSWORD` est vide, le code de vérification s'affiche dans la console au lieu d'être envoyé par email.

```
📧 Code généré pour user@example.com: 123456
⚠ MODE DÉVELOPPEMENT : Email non envoyé (configurez SENDER_PASSWORD)
✓ Code de vérification : 123456
```

## 🔒 Sécurité

- ✅ Le mot de passe d'application est **différent** de votre mot de passe Gmail
- ✅ Vous pouvez le révoquer à tout moment
- ✅ Ne partagez **JAMAIS** ce mot de passe
