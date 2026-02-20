# 🤖 Configuration de l'API IA Groq + LanguageTool (100% GRATUIT)

## Deux APIs = Meilleur Résultat

"| Service | Rôle | Gratuit | Rapidité |
|---------|------|---------|----------|
| **Groq** | Génère texte créatif | ✅ Oui | ⚡ Ultra-rapide |
| **LanguageTool** | Vérifie grammaire | ✅ Oui | ⚡ Instantané |

**Workflow NOUVEAU (2 boutons séparés):**
```
Vous voyez deux boutons côte à côte :

┌─────────────────┐  ┌─────────────────┐
│ 🤖 Générer      │  │ ✓ Vérifier      │
└─────────────────┘  └─────────────────┘

ÉTAPE 1: Cliquez "🤖 Générer"
    ↓
Groq génère un texte créatif & unique
    ↓
Le texte s'affiche dans la zone de commentaires
    ↓

ÉTAPE 2: Cliquez "✓ Vérifier" (optionnel)
    ↓
LanguageTool vérifie la grammaire/orthographe
    ↓
Vous voyez: "✅ Grammaire parfaite! 100% qualité"
ou
Vous voyez: "⚠️ 2 erreurs détectées | 95% qualité"
```

**Avantages:**
- ✅ Générer sans vérifier (plus rapide)
- ✅ Vérifier uniquement si vous le souhaitez
- ✅ Modifier le texte entre génération et vérification
- ✅ Vérifier plusieurs fois si nécessaire

## Étape 1: Configuration Groq

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
   - L'IA va générer un texte et LanguageTool va vérifier - Résultat en 1-2 secondes !

## Étape 2: LanguageTool (Aucune Configuration Requise ✅)

**Bonne nouvelle:** LanguageTool fonctionne **100% automatiquement** sans clé API !

- API cloud gratuite de LanguageTool : https://api.languagetool.org/v2/check
- **Limite:** 20 requêtes/minute (largement suffisant)
- **Aucune inscription requise** - Ça marche directement ! ✅

**Qu'est-ce qu'il fait ?**
Après que Groq génère votre texte, LanguageTool :
1. ✅ Détecte les erreurs grammaticales
2. ✅ Détecte les erreurs d'orthographe
3. ✅ Calcule un score de qualité (0-100%)
4. ✅ Affiche un résumé : "⚠️ 2 erreurs | 95% qualité"

**Exemple de résultat:**
```
Texte généré par Groq:
"Je demande un congé du 20 février au 27 février. Je préparerai une documentation..."

Vérification LanguageTool:
"⚠️ 1 erreur détectée | 97% qualité"
```

## Résumé: L'application va... 🚀

## Résumé: L'application va... 🚀

**Bouton "🤖 Générer":**
1. Groq génère un texte unique et créatif en français
2. Le texte apparaît dans votre formulaire
3. C'est instantané (1-2 secondes) ⚡

**Bouton "✓ Vérifier" (optionnel):**
1. Analyser la grammaire & l'orthographe avec LanguageTool
2. Afficher un score de qualité (0-100%)
3. C'est aussi instantané et gratuit ✅

**Vous pouvez:**
- ✅ Générer et améliorer manuellement sans vérifier
- ✅ Vérifier plusieurs fois si vous modifiez
- ✅ Utiliser l'un ou l'autre ou les deux
- ✅ Ces deux APIs sont **100% GRATUITES** et sans limite

---

## FAQ - LanguageTool

### ❓ L'API LanguageTool fonctionne sans clé ?
**Oui !** C'est une API cloud publique et gratuite. Aucune authentification requise. Elle fonctionne directement.

### ❓ Que faire si LanguageTool est lent ?
- C'est rare, mais si ça arrive, le texte s'affiche quand même sans vérification
- Les logs afficheront : "⚠️ Erreur LanguageTool... texte affiché"

### ❓ Les erreurs détectées sont-elles corrigées ?
- **Non**, LanguageTool affiche seulement les erreurs détectées
- Vous voyez le texte original + le nombre d'erreurs
- Vous pouvez bien sûr éditer le texte à la main après

### ❓ Les 20 requêtes/minute suffisent ?
- **Oui !** Vous ne ferez pas 20 clics/minute sur "Générer"
- Limite très généreuse pour une utilisation normale ✅

### ❓ Comment désactiver la vérification LanguageTool ?
- Elles est toujours activée mais elle ne bloque rien
- Si elle échoue, le texte s'affiche quand même ✅

---

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
