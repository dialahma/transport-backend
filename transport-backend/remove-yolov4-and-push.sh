#!/bin/bash
set -e

echo "🧠 Vérification du dépôt..."
if ! git rev-parse --is-inside-work-tree > /dev/null 2>&1; then
  echo "❌ Ce dossier n'est pas un dépôt git."
  exit 1
fi

REPO_NAME=$(basename "$(pwd)")
BACKUP_DIR="../${REPO_NAME}_backup_before_yolo_removal_$(date +%Y%m%d_%H%M%S)"

echo "📦 Sauvegarde du dépôt complet → $BACKUP_DIR"
cp -a . "$BACKUP_DIR"

echo "🧹 Suppression définitive de yolov4.weights de l’historique Git…"
git filter-repo --force \
  --path "transport-video/src/main/resources/yolo/yolov4.weights" \
  --invert-paths

echo "🛡️ Ajout de yolov4.weights dans .gitignore (si nécessaire)"
echo "transport-video/src/main/resources/yolo/yolov4.weights" >> .gitignore
git add .gitignore
git commit -m "🚫 Ajout yolov4.weights dans .gitignore"

echo "🔗 Reconfiguration du remote GitHub"
git remote remove origin || true
git remote add origin https://github.com/dialahma/transport-backend.git

echo "🚀 Push --force de toutes les branches…"
git push origin --all --force
git push origin --tags --force

echo "✅ Fichier purgé de tout l’historique. Dépôt propre et synchronisé avec GitHub."

