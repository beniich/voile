#!/usr/bin/env bash
set -e

# Ce script aide à générer un keystore pour la version de production Android de Voile.
# Il va créer un keystore et un fichier properties associé.

APP_DIR="packages/android/app"
KEYSTORE_NAME="voile-release-key.keystore"
PROPS_FILE="../keystore.properties" # packages/android/keystore.properties
ALIAS="voile_alias"

echo "🛡️ Voile VPN - Générateur de Keystore de Production"
echo "====================================================="
echo ""

# Vérifie qu'on est bien à la racine du monorepo
if [ ! -d "$APP_DIR" ]; then
  echo "❌ Erreur : Ce script doit être lancé depuis la racine du projet Voile."
  exit 1
fi

if [ -f "$APP_DIR/$KEYSTORE_NAME" ]; then
  echo "⚠️ Un fichier keystore existe déjà dans $APP_DIR/$KEYSTORE_NAME."
  echo "   Générer un nouveau keystore annulera l'ancien."
  echo "   Annulation."
  exit 1
fi

echo "Entrez un mot de passe fort pour le Keystore (ne l'oubliez pas !):"
read -s KS_PASS
echo "Confirmez le mot de passe :"
read -s KS_PASS_CONFIRM

if [ "$KS_PASS" != "$KS_PASS_CONFIRM" ]; then
  echo "❌ Les mots de passe ne correspondent pas."
  exit 1
fi

echo ""
echo "⏳ Génération du keystore en cours..."

# Génération via keytool (nécessite le JDK)
keytool -genkey -v \
  -keystore "$APP_DIR/$KEYSTORE_NAME" \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass "$KS_PASS" \
  -keypass "$KS_PASS" \
  -dname "CN=Voile Team, OU=Engineering, O=Voile VPN, L=Paris, S=Ile-de-France, C=FR"

echo ""
echo "✅ Keystore généré avec succès : $APP_DIR/$KEYSTORE_NAME"

# Création du fichier properties
echo "storePassword=$KS_PASS" > "packages/android/keystore.properties"
echo "keyPassword=$KS_PASS" >> "packages/android/keystore.properties"
echo "keyAlias=$ALIAS" >> "packages/android/keystore.properties"
echo "storeFile=app/$KEYSTORE_NAME" >> "packages/android/keystore.properties"

echo "✅ Fichier de configuration généré : packages/android/keystore.properties"
echo ""
echo "⚠️ IMPORTANT : Ne commitez jamais ces deux fichiers sur Git."
echo "Assurez-vous qu'ils soient bien dans votre .gitignore."
echo ""
echo "Vous pouvez maintenant lancer : cd packages/android && ./gradlew bundleRelease"
