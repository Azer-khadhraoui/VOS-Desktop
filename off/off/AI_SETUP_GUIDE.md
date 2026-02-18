# 🚀 AI Enhancement Setup Guide - FREE Option

## ✅ Quick Setup (5 minutes)

### Step 1: Get Your FREE Google Gemini API Key

1. Go to: **https://aistudio.google.com/app/apikey**
2. Sign in with your Google account (no credit card required!)
3. Click "Create API Key"
4. Copy your API key (starts with something like `AIzaSy...`)

### Step 2: Configure the Application

1. Open: `off/off/src/main/resources/config.properties`
2. Update these two lines:
   ```properties
   ai.provider=gemini
   gemini.api.key=YOUR_KEY_HERE
   ```
3. Replace `AIzaSyDPPMBJS_GlZVEOXK69JHkUOCfBuYd9eHU` with the key you copied
4. Save the file

### Step 3: Restart the Application

- Close your application completely
- Restart it from your IDE or terminal

### Step 4: Test It! 🎉

1. Click "Ajouter Offre" in the dashboard
2. Fill in the job title
3. Enter a job description (can have errors!)
   Example:
   ```
   nous cherchon un developeur pour notre projet
   doit savoir java et spring
   ```
4. Click "✨ Améliorer avec l'IA"
5. Watch the magic happen! ✨

## 🆓 Free Tier Limits

Google Gemini Free Tier:
- **60 requests per minute** (more than enough!)
- **1500 requests per day**
- **No credit card required**
- **No time limit**

This is perfect for your application!

## 🔄 Alternative: Claude (Paid)

If you want to use Claude instead:
```properties
ai.provider=claude
claude.api.key=sk-ant-api03-...
```

Get Claude API key: https://console.anthropic.com/
(Requires payment/credits)

## 🐛 Troubleshooting

**Problem: "Configuration manquante" error**
- Make sure you saved config.properties
- Restart the application
- Check that the API key doesn't have extra spaces

**Problem: "API call failed"**
- Verify your API key is correct
- Check your internet connection
- Make sure you didn't exceed free tier limits

## 📝 Example Enhancement

**Before:**
```
nous rechrchons developpeur java
salaire correct
```

**After (AI Enhanced):**
```
Nous recherchons un développeur Java expérimenté pour rejoindre notre équipe.
Le poste offre un salaire compétitif et d'excellentes opportunités de développement professionnel.
Vous travaillerez sur des projets stimulants dans un environnement collaboratif.
```

Enjoy your FREE AI-powered job description enhancement! 🎉
