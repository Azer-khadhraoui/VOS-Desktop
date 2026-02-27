# 📷 Configuration OpenCV pour la Reconnaissance Faciale

## ✅ Ce qui est déjà fait :

1. **Dépendance ajoutée dans `pom.xml`** - OpenCV 4.12.0 configuré
2. **Service de reconnaissance créé** - `FaceRecognitionService.java` dans `services/`
3. **Interface webcam avec preview** - `WebcamView.fxml` et `WebcamController.java`
4. **Bouton dans SignIn** - "Connexion par reconnaissance faciale" ajouté
5. **Controller modifié** - Méthode `signinWithFace()` implémentée avec popup

---

## 🔧 Configuration dans IntelliJ IDEA :

### Étape 1 : Recharger le projet Maven
1. Ouvre IntelliJ IDEA
2. Clique sur l'icône Maven (à droite) → clic droit sur ton projet → **Reload Maven Project**
3. Attends que les dépendances se chargent

### Étape 2 : Vérifier la structure du projet
1. Dans le panneau "Project", vérifie que tu vois :
   ```
   lib/opencv/build/java/
   ├── opencv-4120.jar
   └── x64/opencv_java4120.dll
   ```

### Étape 3 : Ajouter OpenCV aux bibliothèques (SI NÉCESSAIRE)
Si IntelliJ ne reconnaît pas OpenCV automatiquement :

1. **File** → **Project Structure** (Ctrl+Alt+Shift+S)
2. **Modules** → Sélectionne ton module → **Dependencies**
3. Clique sur **+** → **JARs or directories**
4. Navigue vers `lib/opencv/build/java/opencv-4120.jar`
5. Clique **OK** → **Apply** → **OK**

---

## 🧹 Fichiers à supprimer (optionnel - pour réduire la taille) :

Tu peux supprimer ces dossiers sans impact sur la reconnaissance faciale :

```
lib/opencv/sources/          ← Code source (non nécessaire)
lib/opencv/build/bin/        ← Exécutables (non nécessaire en Java)
lib/opencv/build/python/     ← Bindings Python (non nécessaire)
lib/opencv/build/x64/        ← Bibliothèques C++ (non nécessaire)
lib/opencv/build/etc/lbpcascades/  ← Cascades alternatives (optionnel)
```

**À GARDER ABSOLUMENT :**
```
lib/opencv/build/java/opencv-4120.jar
lib/opencv/build/java/x64/opencv_java4120.dll
lib/opencv/build/etc/haarcascades/haarcascade_frontalface_default.xml
```

---

## 🚀 Comment utiliser :

1. **Lance l'application** depuis IntelliJ (Run MainFX.java)
2. Sur la page de connexion :
   - Entre ton **email** (pour identifier ton compte)
   - Clique sur **"📷 Connexion par reconnaissance faciale"**
3. **Une fenêtre s'ouvre** avec le flux vidéo de ta webcam en temps réel
4. **Positionne ton visage** - un rectangle vert apparaît quand il est détecté
5. **Clique sur "📸 Capturer et Authentifier"**
6. L'application compare avec ta photo de profil et te connecte automatiquement !

---

## ⚠️ PROBLÈME IMPORTANT - Chemin d'image

### 🐛 Symptôme :
```
can't open/read file: check file path/integrity
Image_profil (DB): photo_linkedin_1771108063689.jpg
```

### 🔍 Cause :
Ton image est enregistrée avec juste le **nom du fichier** au lieu du **chemin complet absolu**.

### ✅ Solution (3 étapes) :

**1. Lance le script de diagnostic :**
```powershell
.\fix-image-paths.ps1
```
Copie le chemin absolu affiché (ex: `C:\Users\Azer Khadhraoui\Desktop\VOS-Desktop\gestionutilisateur\images`)

**2. Ouvre phpMyAdmin et exécute :**
```sql
UPDATE utilisateur
SET image_profil = 'C:\Users\Azer Khadhraoui\Desktop\VOS-Desktop\gestionutilisateur\images\photo_linkedin_1771108063689.jpg'
WHERE email = 'mohamedazer.khadhraoui@gmail.com';
```
*(Remplace par TON chemin complet et TON email)*

**3. Vérifie que le fichier existe :**
Assure-toi que `photo_linkedin_1771108063689.jpg` est bien dans le dossier `images/`

### 💡 Note :
Le code de `SignupController.java` a déjà été corrigé pour sauvegarder le chemin absolu.
Les nouveaux comptes créés n'auront pas ce problème.

---

## ⚠️ Prérequis :

- ✅ Une **webcam** connectée et fonctionnelle
- ✅ Une **photo de profil** enregistrée pour ton compte
- ✅ Bon **éclairage** pour la reconnaissance

---

## 🐛 Dépannage :

### Erreur "can't open/read file: check file path/integrity"
→ **Le chemin d'image en base est invalide**
- Vérifie que l'image est enregistrée avec un **chemin absolu complet**
- Exécute `.\fix-image-paths.ps1` pour obtenir le bon chemin
- Mets à jour la base de données avec le chemin complet

### Erreur "UnsatisfiedLinkError" ou "opencv_java4120.dll"
→ **La DLL OpenCV n'est pas trouvée**
- Vérifie que `lib/opencv/build/java/x64/opencv_java4120.dll` existe
- Recharge le projet Maven dans IntelliJ

### Erreur "Impossible d'ouvrir la webcam"
→ **Webcam indisponible**
- Ferme toutes les apps qui utilisent la webcam (Zoom, Teams, Skype...)
- Vérifie que ta webcam fonctionne (test avec l'app Caméra Windows)
- Redémarre l'application

### La fenêtre webcam ne s'ouvre pas
→ **Problème de chargement FXML**
- Vérifie que `WebcamView.fxml` existe dans `src/main/resources/`
- Rebuild le projet (Build → Rebuild Project)

### Rectangle vert n'apparaît pas (visage non détecté)
→ **Problème de détection**
- Améliore l'éclairage (lumière de face, pas de contre-jour)
- Éloigne ou rapproche ton visage de la caméra
- Retire lunettes/masque si applicable

### Erreur "Visage non reconnu" même avec la bonne personne
→ **Seuil de similarité trop élevé**
- Le seuil actuel est à **75%** (dans `WebcamController.java` ligne 229)
- Tu peux le réduire à 70% ou 65% si nécessaire
- Assure-toi que ta photo de profil est claire et bien éclairée

---

## 📊 Comment ça marche :

1. **Ouverture webcam** : Flux vidéo en temps réel à 30 FPS
2. **Détection continue** : Haar Cascade détecte les visages dans chaque frame
3. **Visualisation** : Rectangle vert autour du visage détecté
4. **Capture** : L'utilisateur clique sur "Capturer"
5. **Extraction** : Le visage est extrait et converti en niveaux de gris
6. **Redimensionnement** : 200x200 pixels pour uniformiser
7. **Comparaison** : Différence pixel par pixel avec l'image en base
8. **Calcul similarité** : Pourcentage de correspondance
9. **Authentification** : Si ≥ 75% → Connexion réussie ✅

---

## 🎯 Prochaines améliorations possibles :

- [x] ~~Interface de prévisualisation de la webcam~~ ✅ FAIT !
- [ ] Utiliser un algorithme plus avancé (LBPH Face Recognizer)
- [ ] Détection de "liveness" (anti-spoofing avec photo)
- [ ] Support multi-visages
- [ ] Entraînement sur plusieurs photos du même utilisateur
- [ ] Ajustement automatique de la luminosité

---

**Bon test ! 🚀**
