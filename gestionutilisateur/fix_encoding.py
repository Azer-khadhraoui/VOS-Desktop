# -*- coding: utf-8 -*-
import sys

file_path = r'c:\Users\Azer Khadhraoui\Desktop\VOS-Desktop\gestionutilisateur\src\main\java\controllers\AdminOffresController.java'

# Read file
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix encoding issues
fixes = {
    'Ã©': 'é',
    'Ã¨': 'è',
    'Ã ': 'à',
    'Ã´': 'ô',
    'Ã¢': 'â',
    'Ã§': 'ç',
    'Ã»': 'û',
    'Ã®': 'î',
    'Ã¯': 'ï',
    'Ã‰': 'É',
    'Ã€': 'À',
    'Å'': 'œ',
    'âœ…': '✅',
    'âŒ': '❌',
    'ðŸ"‹': '📋',
    'ðŸŽ¯': '🎯',
    'ðŸ"Š': '📊',
    'ðŸ'¼': '💼',
    'ðŸ"ˆ': '📈',
    'ðŸ"„': '🔄',
    'ðŸ"': '🔍',
    'ðŸ'¤': '👤',
    'ðŸ"…': '📅',
    'ðŸŽ"': '🎓',
    'â­': '⭐',
    'ðŸ¤–': '🤖',
    'âœ¨': '✨',
    'ðŸ"': '📝',
    'ðŸ'¡': '💡',
    'ðŸ"': '🔗',
    'ðŸ'°': '💰',
    'ðŸ—"': '🗓',
    'ðŸ"': '📍',
    'ðŸ ': '🏠',
    'ðŸ¢': '🏢',
    'â†'': '←',
    'âœ"': '✓',
    'â€¦': '…',
    'ðŸ§ª': '🧪',
    'ðŸŽ¨': '🎨',
}

for bad, good in fixes.items():
    content = content.replace(bad, good)

# Write back
with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
    f.write(content)

print('✓ Encodage UTF-8 corrigé pour AdminOffresController.java')
