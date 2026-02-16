# 📷 Reconnaissance Faciale - Guide Rapide

## 🚀 Pour commencer immédiatement :

### 1️⃣ **Recharge Maven dans IntelliJ**
- Ouvre le panneau Maven (à droite)
- Clic droit sur le projet → **Reload Project**

### 2️⃣ **Corrige le chemin d'image en base de données**

**Option A - Script automatique :**
```powershell
.\fix-image-paths.ps1
```
Copie le chemin affiché et utilise-le dans l'étape suivante.

**Option B - Manuellement dans phpMyAdmin :**
```sql
UPDATE utilisateur
SET image_profil = 'C:\Users\Azer Khadhraoui\Desktop\VOS-Desktop\gestionutilisateur\images\photo_linkedin_1771108063689.jpg'
WHERE email = 'mohamedazer.khadhraoui@gmail.com';
```
*(Remplace par TON chemin complet)*

### 3️⃣ **Lance l'application**
```
Run → MainFX.java
```

### 4️⃣ **Teste la reconnaissance faciale**
1. Entre ton email
2. Clique sur **"📷 Connexion par reconnaissance faciale"**
3. Une fenêtre s'ouvre avec ta webcam
4. Positionne ton visage (rectangle vert = détecté)
5. Clique sur **"📸 Capturer et Authentifier"**
6. ✅ Connexion automatique !

---

## 📁 Fichiers importants :

| Fichier | Description |
|---------|-------------|
| `WebcamView.fxml` | Interface de la fenêtre webcam |
| `WebcamController.java` | Logique webcam + reconnaissance |
| `SigninController.java` | Gestion connexion (modifié) |
| `FaceRecognitionService.java` | Service de reconnaissance (legacy) |
| `fix-image-paths.ps1` | Script diagnostic chemins images |
| `fix-image-paths.sql` | Requêtes SQL de correction |
| `RECONNAISSANCE_FACIALE_GUIDE.md` | Guide complet détaillé |

---

## ⚠️ Problèmes fréquents :

| Problème | Solution |
|----------|----------|
| "can't open/read file" | Chemin d'image invalide → Execute `fix-image-paths.sql` |
| Webcam ne s'ouvre pas | Ferme Zoom/Teams, redémarre l'app |
| Visage non détecté | Améliore l'éclairage, rapproche/éloigne |
| Visage non reconnu | Baisse le seuil à 70% (`WebcamController.java` ligne 229) |

---

## 🎯 Fonctionnalités :

- ✅ Preview webcam en temps réel (30 FPS)
- ✅ Détection de visage avec rectangle vert
- ✅ Comparaison avec photo de profil (similarité ≥ 75%)
- ✅ Connexion automatique si reconnu
- ✅ Interface moderne avec animations
- ✅ Gestion des erreurs complète

---

## 📖 Pour plus de détails :
Consulte **RECONNAISSANCE_FACIALE_GUIDE.md**

---

**Bon test ! 🚀📷**
