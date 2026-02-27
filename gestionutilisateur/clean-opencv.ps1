# Script de nettoyage OpenCV - Supprime les fichiers inutiles
# Garde uniquement ce qui est nécessaire pour la reconnaissance faciale en Java

$opencvPath = "lib/opencv"

Write-Host "🧹 Nettoyage des fichiers OpenCV inutiles..." -ForegroundColor Cyan

# Fichiers et dossiers à supprimer
$toDelete = @(
    "$opencvPath/sources",
    "$opencvPath/build/bin",
    "$opencvPath/build/python",
    "$opencvPath/build/x64",
    "$opencvPath/build/etc/lbpcascades",
    "$opencvPath/build/java/x86",
    "$opencvPath/build/include"
)

$savedSpace = 0

foreach ($item in $toDelete) {
    if (Test-Path $item) {
        $size = (Get-ChildItem -Path $item -Recurse -File | Measure-Object -Property Length -Sum).Sum
        $savedSpace += $size
        
        Remove-Item -Path $item -Recurse -Force
        Write-Host "✓ Supprimé: $item" -ForegroundColor Green
    } else {
        Write-Host "⊘ Déjà absent: $item" -ForegroundColor Yellow
    }
}

$savedSpaceMB = [math]::Round($savedSpace / 1MB, 2)
Write-Host ""
Write-Host "✅ Nettoyage terminé !" -ForegroundColor Green
Write-Host "💾 Espace libéré: $savedSpaceMB MB" -ForegroundColor Cyan
Write-Host ""
Write-Host "📦 Fichiers conservés:" -ForegroundColor Yellow
Write-Host "  - lib/opencv/build/java/opencv-4120.jar" -ForegroundColor White
Write-Host "  - lib/opencv/build/java/x64/opencv_java4120.dll" -ForegroundColor White
Write-Host "  - lib/opencv/build/etc/haarcascades/" -ForegroundColor White
