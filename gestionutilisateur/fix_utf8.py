# -*- coding: utf-8 -*-
"""
Script to fix UTF-8 encoding issues in AdminOffresController.java
"""

import sys
import os

# Force UTF-8 output
if sys.stdout.encoding != 'utf-8':
    sys.stdout.reconfigure(encoding='utf-8')

file_path = r"c:\Users\Azer Khadhraoui\Desktop\VOS-Desktop\gestionutilisateur\src\main\java\controllers\AdminOffresController.java"

# Mapping of malformed characters to correct characters
replacements = {
    # French characters
    "éÂ©": "é",
    "éÂ¨": "è",
    "éÂª": "ê",
    "Ã©": "é",
    "Ã¨": "è",
    "Ã§": "ç",
    "Ã": "à",
    "Ã¢": "â",
    "Ã´": "ô",
    "Ã»": "û",
    "Ã¯": "ï",
    "Ã¼": "ü",
    "Ã¦": "æ",
    "Å": "œ",
    "Ã‰": "É",
    "Ãˆ": "È",
    "Ã‡": "Ç",
    "Ã€": "À",
    
    # Emojis and special characters
    "é°Å¸âÂ": "📋",
    "é°Å¸ââ¹": "📝",
    "é°Å¸âÂ¼": "📄",
    "é°Å¸âÅ ": "📌",
    "é°Å¸ÂÂ¢": "💢",
    "é°Å¸âÂ": "📍",
    "é°Å¸âÂ¤": "👤",
    "é¢ÅÂ¨": "✨",
    "é¢ÂÅ": "❌",
    "é¢Åâ¦": "✅",
    "é¢ÅÂ": "⚠",
    "éâ°": "Ã©",  # This might be a double encoding issue
    
    # Other patterns
    "Mettre éÂ  jour": "Mettre à jour",
    "améÂ©lioréÂ©e": "améliorée",
    "améÂ©liorer": "améliorer",
    "améÂ©lioration": "amélioration",
    "AméÂ©liorer": "Améliorer",
    "AméÂ©lioration": "Amélioration",
    "configu réÂ©e": "configurée",
    "DéÂ©finissez": "Définissez",
    "cléÂ©": "clé",
    "apréÂ¨s": "après",
    "RedéÂ©marrez": "Redémarrez",
    "déÂ©passer": "dépasser",
    "caractéÂ¨res": "caractères",
    "DéÂ©crivez": "Décrivez",
    "IntéÂ©rim": "Intérim",
    "Sé©lectionnez": "Sélectionnez",
    "séÂ©lectionné": "sélectionné",
    "créÂ©é": "créé",
    "créÂ©er": "créer",
    "généÂ©rer": "générer",
    "généÂ©réÂ©": "généré",
    "IntéÂ©gration": "Intégration",
    "préÂ©f éÂ©rence": "préférence",
    "PréÂ©féÂ©rence": "Préférence",
    "préÂªtre": "être",
    "éÂªtre": "être",
    "Féâ°": "FÉ",
    "éâ°": "É",
    "PRéâ°Féâ°RENCE": "PRÉFÉRENCE",
}

try:
    # Read file with different encodings
    content = None
    for encoding in ['utf-8', 'latin-1', 'cp1252', 'iso-8859-1']:
        try:
            with open(file_path, 'r', encoding=encoding) as f:
                content = f.read()
            print(f"File read successfully with encoding: {encoding}")
            break
        except UnicodeDecodeError:
            continue
    
    if content is None:
        print("ERROR: Could not read file with any encoding")
        sys.exit(1)
    
    # Apply replacements
    original_content = content
    for wrong, correct in replacements.items():
        if wrong in content:
            count = content.count(wrong)
            print(f"Replacing '{wrong}' -> '{correct}' ({count} occurrences)")
            content = content.replace(wrong, correct)
    
    # Write back with UTF-8
    with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
        f.write(content)
    
    if content != original_content:
        print("\n✅ File successfully fixed and saved with UTF-8 encoding!")
    else:
        print("\n⚠ No changes made (no malformed characters found)")
    
except Exception as e:
    print(f"ERROR: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)
