# Guide utilisateur Voile

## Premiers pas

### 1. Installation

**Web** : Ouvrez [voile.app](https://voile.app) dans Chrome, Edge, ou Firefox. Cliquez sur "Ajouter à l'écran d'accueil" pour l'installer comme une app.

**Android** : Téléchargez l'APK depuis notre site ou le Play Store.

### 2. Création de compte

Vous pouvez vous connecter de deux façons :

- **Magic link** : entrez votre email, recevez un lien, cliquez dessus.
- **Google / Apple** : connexion OAuth en un clic.

Aucun mot de passe à retenir.

### 3. Connexion

1. Choisissez un serveur (7 pays disponibles).
2. Cliquez sur le bouton central.
3. Sur Android : autorisez la permission VPN (une seule fois).
4. Attendez 3-5 secondes : le sonar passe de pulsations rapides à une respiration lente. ✅ Vous êtes protégé.

## Comprendre l'interface

### Status pill (en haut)

| État | Couleur | Signification |
|---|---|---|
| Déconnecté | Gris | Pas de tunnel actif |
| Négociation | Orange pulsant | Handshake en cours |
| Sécurisé | Turquoise | Tunnel actif |
| Erreur | Rouge | Problème — réessayez |

### Trust Score

Une note de 0 à 100 (A/B/C/D) qui évalue votre niveau de protection :

- **A (90-100)** : tout va bien
- **B (75-89)** : attention mineure (géoloc exposée, par ex.)
- **C (60-74)** : fuite DNS probable ou latence élevée
- **D (<60)** : problème grave détecté

Cliquez sur le badge pour voir le détail des problèmes.

### Avant/Après IP

Quand vous êtes connecté, l'écran principal affiche :
- **Votre nouvelle IP** (via Cloudflare edge)
- **Votre ancienne IP** (barrée)

Cela permet de vérifier visuellement que votre IP a bien changé.

## Réglages

### Protocole

- **WireGuard** (recommandé) : plus rapide, plus moderne.
- **OpenVPN** : plus compatible avec les réseaux restreints (hôtels, aéroports).

### Protection

- **Kill Switch** : bloque internet si le tunnel tombe (évite les fuites).
- **Connexion automatique** : se reconnecte au démarrage du téléphone.
- **CyberSec Shield** : bloque DNS menteurs et traqueurs.

### Split tunneling

Choisissez quelles **apps** contournent le VPN (utilisent votre connexion directe).
Utile pour :
- Apps bancaires qui refusent les IP Cloudflare
- Streaming local qui n'a pas besoin d'être anonymisé

⚠️ Les apps exclues ne sont **pas protégées**.

## FAQ

**Q : Pourquoi 7 serveurs seulement ?**

R : Contrairement aux VPN commerciaux, Voile ne contrôle pas ses serveurs. Nous utilisons l'infrastructure Cloudflare, donc le serveur le plus proche est toujours optimal.

**Q : WARP bloque-t-il Netflix / Disney+ ?**

R : Cloudflare est souvent blacklisté par les services de streaming géo-restreints. Pour cet usage, préférez un VPN avec IP résidentielle (NordVPN, Surfshark, etc.).

**Q : Mes données sont-elles vraiment privées ?**

R : Cloudflare voit votre trafic chiffré mais ne le **stocke pas** selon leur politique de confidentialité. Pour un anonymat total, combinez Voile + Tor.

**Q : Pourquoi mon ping est-il élevé ?**

R : Vous êtes probablement loin du serveur Cloudflare le plus proche. Le Trust Score vous alertera si la latence dépasse 150ms.

## Support

- 📧 Email : support@voile.app
- 🐛 Issues : github.com/voile/voile/issues
- 💬 Discord : discord.gg/voile
