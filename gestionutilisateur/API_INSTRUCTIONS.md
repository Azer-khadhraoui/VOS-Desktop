# 🤖 Configuration de l'API IA Groq (100% GRATUIT)

## Pourquoi Groq ?

✅ **100% GRATUIT** - Pas de carte bancaire requise  
✅ **Rapide** - Inférence ultra-rapide (jusqu'à 750 tokens/sec)  
✅ **Puissant** - Modèles LLaMA 3.3 70B de haute qualité  
✅ **Libre d'utilisation** - Rate limit gratuit généreux (30 requêtes/min)  
✅ **Pas de frais cachés** - Vraiment gratuit, pas d'essai limité

## Étapes d'installation

### 1️⃣ Créer un compte Groq

1. Allez sur: **https://console.groq.com**
2. Cliquez sur **"Sign Up"** (ou **"Get Started"**)
3. Inscrivez-vous avec:
   - Google
   - GitHub
   - Ou email/mot de passe

### 2️⃣ Obtenir votre clé API

1. Une fois connecté, allez dans **"API Keys"** (menu de gauche)
2. Cliquez sur **"Create API Key"**
3. Donnez un nom à votre clé (ex: "VOS-Desktop")
4. Cliquez sur **"Submit"**
5. **COPIEZ IMMÉDIATEMENT** votre clé (format: `gsk_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`)
   ⚠️ Vous ne pourrez plus la voir après !

### 3️⃣ Configurer l'application

1. Ouvrez le fichier:
   ```
   src/main/java/services/AITextGeneratorService.java
   ```

2. Trouvez la ligne 8:
   ```java
   private static final String GROQ_API_KEY = "gsk_votre_cle_ici";
   ```

3. Remplacez `gsk_votre_cle_ici` par votre vraie clé:
   ```java
   private static final String GROQ_API_KEY = "gsk_abc123xyz456...";
   ```

4. Sauvegardez le fichier (Ctrl+S)

### 4️⃣ Compiler et tester

1. Recompilez le projet:
   ```bash
   mvn clean compile
   ```

2. Lancez l'application:
   ```bash
   mvn javafx:run
   ```

3. Testez la génération:
   - Allez dans **"Demandes de congé"** ou **"Démissions"**
   - Remplissez les champs
   - Cliquez sur **"🤖 Générer"**
   - L'IA devrait générer un texte unique en quelques secondes !

## Modèles disponibles (tous GRATUITS)

| Modèle                        | Description                       | Vitesse   |
|-------------------------------|-----------------------------------|-----------|
| `llama-3.3-70b-versatile`     | ✅ **RECOMMANDÉ** - Équilibré     | Très rapide |
| `llama-3.1-70b-versatile`     | Polyvalent, bonne qualité         | Rapide    |
| `llama-3.1-8b-instant`        | Ultra-rapide, moins créatif       | Ultra-rapide |
| `mixtral-8x7b-32768`          | Long contexte (32K tokens)        | Rapide    |

## Limites du plan gratuit

- **30 requêtes/minute** pour llama-3.3-70b-versatile
- **14,400 tokens/minute** de sortie
- **Pas de limite quotidienne** ✅
- **Pas d'expiration** ✅

C'est largement suffisant pour une utilisation normale !

## Dépannage

### ❌ Erreur 401 Unauthorized
**Solution:** Vérifiez que vous avez bien copié-collé votre clé API complète (commence par `gsk_`)

### ❌ Erreur 429 Too Many Requests
**Solution:** Vous avez dépassé 30 requêtes/minute. Attendez 1 minute.

### ❌ Erreur 400 Bad Request
**Solution:** Vérifiez les logs pour voir le message d'erreur détaillé. Peut-être un problème de format.

### ❌ "Réponse vide de l'API"
**Solution:** Le modèle n'a peut-être pas pu générer de texte. Réessayez ou changez le prompt.

### ❌ Textes toujours identiques
**Solution:** Groq utilise `temperature=1.0` pour maximiser la créativité. Si les textes se répètent, c'est probablement un problème avec le modèle lui-même (réessayez plus tard).

## Liens utiles

- 🌐 Console Groq: https://console.groq.com
- 📚 Documentation API: https://console.groq.com/docs/quickstart
- 💬 Support: https://console.groq.com/support
- 🎯 Playground (tester les modèles): https://console.groq.com/playground

## Comparaison avec autres APIs

| API              | Gratuit ? | Limite              | Vitesse      |
|------------------|-----------|---------------------|--------------|
| **Groq** ✅      | ✅ Oui    | 30 req/min          | Ultra-rapide |
| OpenAI ChatGPT   | ❌ Non    | 5$/mois minimum     | Rapide       |
| Google Gemini    | ⚠️ Limité | 60 req/min (puis $) | Moyen        |
| xAI Grok         | ❌ Non    | 0.30$/million tok   | Rapide       |
| Hugging Face     | ❌ API obsolète | -             | -            |

---

**🎉 Vous êtes prêt ! L'IA va maintenant générer des textes uniques et créatifs pour vos demandes de congé et démissions !**
