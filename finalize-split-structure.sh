#!/bin/bash

set -e

echo "🚀 Finalisation de la séparation backend/frontend..."

# RÉPERTOIRES DE BASE
BACKEND_DIR="../transport-backend"
FRONTEND_DIR="../transport-frontend"
TMP_DIR="finalize-tmp"

mkdir -p "$TMP_DIR"

# 1. VÉRIF RÉPERTOIRES .git
if [[ ! -d "$BACKEND_DIR/.git" || ! -d "$FRONTEND_DIR/.git" ]]; then
  echo "❌ Les dépôts Git doivent exister dans $BACKEND_DIR et $FRONTEND_DIR"
  exit 1
fi

# 2. BACKEND - RÉORGANISATION
echo "📦 Réorganisation du backend..."
mkdir -p "$BACKEND_DIR/transport-core" "$BACKEND_DIR/transport-video" "$BACKEND_DIR/transport-security" "$BACKEND_DIR/transport-api-gateway"

mv "$BACKEND_DIR/src" "$BACKEND_DIR/transport-core/" 2>/dev/null || true
mv "$BACKEND_DIR/transport-video/"* "$BACKEND_DIR/transport-video/" 2>/dev/null || true
mv "$BACKEND_DIR/transport-security/"* "$BACKEND_DIR/transport-security/" 2>/dev/null || true
mv "$BACKEND_DIR/transport-api-gateway/"* "$BACKEND_DIR/transport-api-gateway/" 2>/dev/null || true

# 3. FRONTEND - RÉORGANISATION
echo "🎨 Réorganisation du frontend..."
mkdir -p "$FRONTEND_DIR/frontend"
mv "$FRONTEND_DIR/src" "$FRONTEND_DIR/frontend/" 2>/dev/null || true
mv "$FRONTEND_DIR/angular.json" "$FRONTEND_DIR/frontend/" 2>/dev/null || true
mv "$FRONTEND_DIR/package.json" "$FRONTEND_DIR/frontend/" 2>/dev/null || true
mv "$FRONTEND_DIR/tsconfig.json" "$FRONTEND_DIR/frontend/" 2>/dev/null || true
mv "$FRONTEND_DIR/vite.config.ts" "$FRONTEND_DIR/frontend/" 2>/dev/null || true

# 4. RÉORGANISATION DES .git IGNORE
echo "🛡️ Vérification des .gitignore..."
echo -e "\n# Ignore backups and temp\nfinalize-tmp/\n" >> "$BACKEND_DIR/.gitignore"
echo -e "\n# Ignore backups and temp\nfinalize-tmp/\n" >> "$FRONTEND_DIR/.gitignore"

# 5. COMMIT FINAL (facultatif)
read -p "💬 Souhaitez-vous faire un commit de réorganisation dans chaque repo ? [y/N]: " do_commit
if [[ "$do_commit" =~ ^[Yy]$ ]]; then
  (
    cd "$BACKEND_DIR"
    git add .
    git commit -m "🔧 Réorganisation du backend (structure modules)"
  )
  (
    cd "$FRONTEND_DIR"
    git add .
    git commit -m "🎨 Réorganisation du frontend (structure propre)"
  )
fi

# 6. RAPPORT
echo -e "\n📄 État final :"
echo -e "\n🟦 Backend:"
cd "$BACKEND_DIR"
git status
echo -e "\n🟨 Frontend:"
cd "$FRONTEND_DIR"
git status

echo -e "\n✅ Finalisation terminée avec succès."

