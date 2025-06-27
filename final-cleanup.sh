#!/bin/bash
set -e

cd /app/workspace/java/transport-backup-2025-06-26-1817

echo "📦 Backup → transport-backup-before-final-purge"
cp -r . ../transport-backup-before-final-purge

echo "🧹 Suppression de yolov4.weights du vrai chemin dans l’historique"
git remote remove origin || true
git filter-repo --force \
  --path "transport-backend/transport-video/src/main/resources/yolo/yolov4.weights" \
  --invert-paths

echo "🛡️ Ajout au .gitignore"
echo "transport-backend/transport-video/src/main/resources/yolo/yolov4.weights" >> .gitignore
git add .gitignore
git commit -m "🚫 Ajout yolov4.weights dans .gitignore"

echo "🔗 Ajout du remote"
git remote add origin https://github.com/dialahma/transport-backend.git

echo "🚀 Push complet"
git push origin --all --force
git push origin --tags --force

