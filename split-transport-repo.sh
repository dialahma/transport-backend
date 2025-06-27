#!/bin/bash
# 🧠 Script pour extraire automatiquement transport-backend et transport-frontend en deux dépôts Git séparés

set -e
REPO_ROOT="$(pwd)"
GITHUB_USER="dialahma"
GITHUB_TOKEN="${GITHUB_TOKEN:-}"
BACKEND_DIR="transport-backend"
FRONTEND_DIR="transport-frontend"

function create_repo() {
  local repo_name=$1
  echo -e "\n🌐 Création du repo distant GitHub : $repo_name"
  if [ -z "$GITHUB_TOKEN" ]; then
    echo "❌ GITHUB_TOKEN non défini. Création du repo distant ignorée."
    return 1
  fi
  curl -s -H "Authorization: token $GITHUB_TOKEN" \
       -d "{\"name\":\"$repo_name\"}" \
       https://api.github.com/user/repos | grep -q "\"full_name\":"
  if [ $? -eq 0 ]; then
    echo "✅ Repo $repo_name créé (ou déjà existant)"
    return 0
  else
    echo "⚠️  Échec ou repo déjà existant : $repo_name"
    return 1
  fi
}

function extract_repo() {
  local subdir=$1
  local target_dir="../$subdir"
  local remote_url="https://github.com/$GITHUB_USER/$subdir.git"

  echo -e "\n📦 Extraction de $subdir dans $target_dir..."

  # Supprimer si le dossier cible existe déjà
  if [ -d "$target_dir" ]; then
    echo "🗑️  Suppression du dossier existant : $target_dir"
    rm -rf "$target_dir"
  fi

  mkdir -p "$target_dir"
  cd "$target_dir"

  git init -b main
  git remote add origin "$remote_url"

  # Importer uniquement les fichiers du sous-dossier
  git fetch "$REPO_ROOT" main --no-tags
  git sparse-checkout init --cone
  git sparse-checkout set "$subdir"
  git pull "$REPO_ROOT" main

  # Déplacer les fichiers à la racine
  mv "$subdir"/* .
  rm -rf "$subdir"

  # Ajouter README et .gitignore
  echo "# $subdir" > README.md
  echo -e "node_modules/\ntarget/\n.gitignore\n*.iml" > .gitignore

  git add .
  git commit -m "🎉 Initialisation du repo $subdir depuis $REPO_ROOT"
  git branch -M main

  echo "🚀 Push vers GitHub : $remote_url"
  git push -u origin main --force

  cd "$REPO_ROOT"
}

# --- Étapes principales ---
echo "🧭 Début de la séparation du dépôt"

create_repo "$BACKEND_DIR"
extract_repo "$BACKEND_DIR"

create_repo "$FRONTEND_DIR"
extract_repo "$FRONTEND_DIR"

echo -e "\n✅ Tous les sous-répertoires ont été extraits et poussés avec succès dans des dépôts GitHub indépendants."

