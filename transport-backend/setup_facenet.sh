#!/bin/bash

# Script modifié pour facenet.pb avec gestion des erreurs

echo "🔹 Création de l'environnement virtuel Python..."
python3 -m venv venv

echo "🔹 Activation de l'environnement virtuel..."
source venv/bin/activate

echo "🔹 Installation de gdown (pour vérification des permissions)..."
pip install gdown --quiet

echo "🔹 Tentative de téléchargement du modèle facenet.pb..."
if gdown https://drive.google.com/uc?id=1t2r2A9Fv2W3rhz6xmt_dzMG6L2H2PG2J -O facenet.pb; then
    echo "✅ Téléchargement réussi"
    
    echo "🔹 Déplacement vers transport-video/src/main/resources/models/"
    mkdir -p transport-video/src/main/resources/models/
    mv facenet.pb transport-video/src/main/resources/models/
    
    echo "🔹 Vérification du fichier..."
    file transport-video/src/main/resources/models/facenet.pb
else
    echo "❌ Échec du téléchargement automatique"
    echo ""
    echo "🔎 SOLUTION MANUELLE REQUISE :"
    echo "1. Ouvrez ce lien dans votre navigateur :"
    echo "   https://drive.google.com/uc?id=1t2r2A9Fv2W3rhz6xmt_dzMG6L2H2PG2J"
    echo "2. Téléchargez manuellement le fichier"
    echo "3. Placez-le dans : transport-video/src/main/resources/models/facenet.pb"
    echo ""
    read -p "Appuyez sur Entrée une fois l'opération manuelle terminée..."
fi

echo "🔹 Nettoyage..."
deactivate

if [ -f "transport-video/src/main/resources/models/facenet.pb" ]; then
    echo "🎉 facenet.pb est bien placé !"
    file transport-video/src/main/resources/models/facenet.pb
else
    echo "⚠️  Le fichier facenet.pb n'est pas présent au bon emplacement"
    echo "Veuillez suivre les instructions ci-dessus pour le placer manuellement"
fi

