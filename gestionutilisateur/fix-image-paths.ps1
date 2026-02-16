# Script pour vérifier et corriger les chemins d'images dans la base de données
# À exécuter si tu as des erreurs "can't open/read file"

Write-Host "🔍 Vérification des chemins d'images dans la base de données" -ForegroundColor Cyan
Write-Host ""

$imagesFolder = "images"

if (-not (Test-Path $imagesFolder)) {
    Write-Host "⚠ Le dossier 'images' n'existe pas dans le projet" -ForegroundColor Yellow
    Write-Host "📁 Création du dossier..." -ForegroundColor Cyan
    New-Item -ItemType Directory -Path $imagesFolder | Out-Null
}

$absolutePath = (Resolve-Path $imagesFolder).Path
Write-Host "📂 Dossier images (chemin absolu): $absolutePath" -ForegroundColor Green
Write-Host ""

Write-Host "📋 Instructions pour corriger la base de données:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Ouvre phpMyAdmin ou MySQL Workbench" -ForegroundColor White
Write-Host ""
Write-Host "2. Exécute cette requête SQL pour voir les chemins actuels:" -ForegroundColor White
Write-Host "   SELECT id_utilisateur, nom, prenom, email, image_profil FROM utilisateur;" -ForegroundColor Cyan
Write-Host ""
Write-Host "3. Pour chaque utilisateur avec un chemin relatif (ex: 'photo.jpg')," -ForegroundColor White
Write-Host "   exécute cette requête pour le corriger:" -ForegroundColor White
Write-Host ""
Write-Host "   UPDATE utilisateur" -ForegroundColor Cyan
Write-Host "   SET image_profil = '$absolutePath\nom_du_fichier.jpg'" -ForegroundColor Cyan
Write-Host "   WHERE id_utilisateur = 1;" -ForegroundColor Cyan
Write-Host ""
Write-Host "   Exemple pour ton compte:" -ForegroundColor Yellow
Write-Host "   UPDATE utilisateur" -ForegroundColor Cyan
Write-Host "   SET image_profil = '$absolutePath\photo_linkedin_1771108063689.jpg'" -ForegroundColor Cyan
Write-Host "   WHERE email = 'mohamedazer.khadhraoui@gmail.com';" -ForegroundColor Cyan
Write-Host ""
Write-Host "4. Vérifie que les fichiers images sont bien dans:" -ForegroundColor White
Write-Host "   $absolutePath" -ForegroundColor Green
Write-Host ""

# Lister les fichiers dans le dossier images
$imageFiles = Get-ChildItem -Path $imagesFolder -File -ErrorAction SilentlyContinue

if ($imageFiles.Count -gt 0) {
    Write-Host "📸 Fichiers trouvés dans le dossier images:" -ForegroundColor Green
    foreach ($file in $imageFiles) {
        $fullPath = $file.FullName
        Write-Host "   ✓ $fullPath" -ForegroundColor White
    }
} else {
    Write-Host "⚠ Aucun fichier trouvé dans le dossier images" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "💡 ASTUCE: Pour éviter ce problème à l'avenir," -ForegroundColor Yellow
Write-Host "   assure-toi que SignupController sauvegarde le chemin ABSOLU" -ForegroundColor Yellow
Write-Host "   et non juste le nom du fichier." -ForegroundColor Yellow
Write-Host ""
