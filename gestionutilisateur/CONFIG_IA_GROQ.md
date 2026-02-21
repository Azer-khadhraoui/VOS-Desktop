# Configuration de l'IA pour la génération de critères d'offres

## 🚀 GROQ API - 100% GRATUIT (Recommandé)

L'IA utilise maintenant **Groq API** qui est **GRATUIT** et **très rapide**.

### Étapes pour obtenir votre clé API gratuite :

1. **Créer un compte Groq** (gratuit)
   - Allez sur : https://console.groq.com/
   - Cliquez sur "Sign Up" ou "Login with Google"
   - Vérifiez votre email

2. **Obtenir votre clé API**
   - Une fois connecté, allez sur : https://console.groq.com/keys
   - Cliquez sur "Create API Key"
   - Donnez un nom à votre clé (ex: "VOS-JobOffers")
   - **COPIEZ la clé** (elle commence par `gsk_...`)
   - ⚠️ **IMPORTANT** : Sauvegardez cette clé, elle ne sera affichée qu'une seule fois!

3. **Configurer l'application**
   - Ouvrez le fichier : `src/main/resources/config.properties`
   - Remplacez la ligne :
     ```properties
     groq.api.key.offres=gsk_YOUR_GROQ_API_KEY_HERE
     ```
   - Par votre vraie clé :
     ```properties
     groq.api.key.offres=gsk_abc123xyz...
     ```

4. **Redémarrer l'application**
   - Relancez l'application JavaFX
   - L'IA fonctionnera maintenant! 🎉

### ✅ Avantages de Groq

- ✅ **100% GRATUIT** (pas de carte de crédit requise)
- ✅ **Très rapide** (inférence ultra-rapide)
- ✅ **Genereux** (500,000 tokens/jour gratuits)
- ✅ **Modèle puissant** : Llama 3.3 70B
- ✅ **Parfait pour la génération de critères de jobs**

### 🧪 Test de l'IA

Une fois configurée :
1. Allez dans la gestion des offres (admin)
2. Cliquez sur "Ajouter Critère"
3. Remplissez : Titre du poste, Niveau d'expérience, Niveau d'éducation
4. Cliquez sur **"Générer avec l'IA"** 🤖
5. Les responsabilités et compétences seront générées automatiquement!

---

## 🔧 Alternative : Google Gemini (aussi gratuit)

Si vous préférez utiliser Google Gemini :

1. Obtenez une clé API gratuite : https://aistudio.google.com/app/apikey
2. Dans `config.properties`, changez :
   ```properties
   ai.provider.offres=gemini
   gemini.api.key=VOTRE_CLE_ICI
   ```

---

## ⚠️ Note importante

Le mode "demo" statique a été **supprimé** selon votre demande. L'IA utilise maintenant uniquement les API réelles de votre ami (Groq, Gemini, ou Claude).

Si aucune clé API n'est configurée, vous verrez un message d'erreur vous demandant de configurer l'API.

---

## 💰 Limites gratuites

| Provider | Gratuit? | Limite quotidienne |
|----------|----------|-------------------|
| **Groq** | ✅ Oui | 500K tokens/jour |
| **Gemini** | ✅ Oui | 1500 requêtes/jour |
| Claude | ❌ Payant | 5$ minimum |

**Recommandation** : Utilisez Groq ou Gemini (tous deux gratuits et excellents).
