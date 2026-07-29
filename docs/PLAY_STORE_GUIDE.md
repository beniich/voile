# Voile VPN — Guide de publication Google Play Store

Ce playbook décrit pas à pas comment signer, packager et publier l'application Android Voile sur le Google Play Store.

## 1. Génération du Keystore

Pour publier une application, vous devez la signer avec une clé cryptographique privée. **Ne commitez jamais cette clé dans Git.**

1. Exécutez le script utilitaire à la racine du projet :
   ```bash
   bash scripts/generate-keystore.sh
   ```
2. Remplissez les informations demandées (mot de passe, nom, organisation).
3. Le script va créer un fichier `voile-release-key.keystore` dans `packages/android/app/`. (Ce fichier est ignoré par Git).
4. Le script va également créer un fichier `packages/android/keystore.properties` contenant vos mots de passe. **Ce fichier ne doit pas être commité.**

## 2. Compilation de l'App Bundle (AAB)

Le Google Play Store requiert désormais le format AAB (Android App Bundle) au lieu des fichiers APK traditionnels.

1. Allez dans le dossier Android :
   ```bash
   cd packages/android
   ```
2. Lancez le build de release :
   ```bash
   ./gradlew bundleRelease
   ```
3. Une fois terminé, votre bundle sera disponible ici :
   `packages/android/app/build/outputs/bundle/release/app-release.aab`

## 3. Configuration de la Google Play Console

### Créer l'application
1. Connectez-vous à la [Google Play Console](https://play.google.com/console).
2. Cliquez sur **Créer une application**.
3. Nom : **Voile VPN** (ou le nom public choisi).
4. Type : **Application**, Catégorie : **Outils** ou **Productivité**.
5. Cochez les déclarations requises (Loi sur l'exportation US, etc.).

### Questionnaire de classification du contenu (Content Rating)
- Catégorie : **Outils / Utilitaires**.
- L'application contient-elle de la violence, des grossièretés, des achats in-app ? (Répondez honnêtement, généralement "Non" pour tout sauf pour les achats in-app si vous activez Stripe/Play Billing plus tard).
- Résultat attendu : PEGI 3 / Tout public.

### Sécurité des données (Data Safety)
C'est la partie la plus importante pour un VPN.
1. **L'application collecte-t-elle des données ?**
   - Oui (Adresse IP, Crash logs via Sentry).
2. **Les données sont-elles chiffrées en transit ?**
   - Oui (100% du trafic via WireGuard/TLS).
3. **Pouvons-nous supprimer les données ?**
   - Oui (le user peut supprimer son compte).
4. Précisez que l'adresse IP est traitée (pour Cloudflare WARP) mais pas stockée de manière persistante ou liée à l'identité réelle à des fins marketing.

### VpnService Policy
Google est très strict avec l'API `VpnService`.
1. Dans "Contenu de l'application", vous devrez remplir la déclaration **VpnService**.
2. Expliquez que Voile est une application dont le but *principal* est d'être un réseau privé virtuel.
3. Affirmez que vous n'insérez pas de publicités dans le trafic web des utilisateurs et que vous ne revendez pas leurs données.

## 4. Fiche Play Store (Store Listing)

### Assets graphiques requis
- **Icône haute résolution** : 512 x 512 px (format PNG ou JPEG). Utilisez le bouclier (logo de l'app).
- **Graphique de fonctionnalité** : 1024 x 500 px. Un visuel large accrocheur avec le nom de l'app.
- **Captures d'écran pour téléphone** : Minimum 2 (ex: L'écran principal avec le Sonar et la sélection des serveurs).

### Textes
- **Description courte** (80 car.) : "Un VPN rapide, transparent et sécurisé propulsé par Cloudflare."
- **Description longue** (4000 car.) : Mettez en avant le Trust Score, la vitesse de WireGuard, l'absence de logs et le design élégant.

## 5. Upload et Publication

1. Allez dans **Production** (ou "Tests internes" si vous voulez tester d'abord).
2. Créez une **Nouvelle release**.
3. Uploadez le fichier `app-release.aab` généré à l'étape 2.
4. Remplissez les notes de version (Release notes).
5. Sauvegardez, passez en revue et **Déployez**.

Le processus de validation initial (Review) par Google prend généralement entre 3 et 7 jours pour un VPN.
