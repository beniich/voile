<div align="center">

# 🛡️ Voile VPN

**VPN transparent propulsé par Cloudflare WARP.**

Disponible en **PWA** (web) et **Android natif** (Kotlin/Compose).

[![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![CI Status](https://img.shields.io/github/actions/workflow/status/voile/voile/ci.yml?branch=main&label=CI)](https://github.com/voile/voile/actions)
[![Deploy Status](https://img.shields.io/github/actions/workflow/status/voile/voile/deploy-cloudflare.yml?branch=main&label=deploy)](https://github.com/voile/voile/actions)
[![Web](https://img.shields.io/badge/web-voile.ricecloud.com-26D9C4)](https://voile.ricecloud.com)

[Démo web](https://voile.ricecloud.com) · [Play Store](#) · [Documentation](./docs) · [Signaler un bug](https://github.com/voile/voile/issues)

</div>

---

## ✨ Fonctionnalités

| Feature | PWA | Android |
|:---|:---:|:---:|
| UI sonar/breathe animations | ✅ | ✅ |
| Authentification (magic link + OAuth) | ✅ | ✅ |
| Persistance locale | ✅ | ✅ |
| Trust Score temps réel | ✅ | ✅ |
| Historique de sessions | ✅ | ✅ |
| Split tunneling | ⚠️ UI only | ✅ Natif |
| **Tunnel chiffré réel** | ⚠️ Browser proxy | ✅ **WireGuard** |
| Kill Switch | ✅ UI | ✅ Système |
| WorkManager background | — | ✅ |

**Différenciateur** : Voile utilise l'infrastructure Cloudflare WARP pour offrir un VPN gratuit, rapide et auditable, sans serveur custom à maintenir.

---

## 🏗️ Architecture

```
voile/
├── packages/
│   ├── core/          # Logique partagée (tokens, types, algorithms)
│   ├── web/           # PWA React + Vite + Workbox
│   └── android/       # App Kotlin + Compose + VpnService
├── supabase/          # Migrations SQL + Edge Functions
├── docs/              # Documentation technique
└── .github/workflows/ # CI/CD
```

### Stack technique

| Couche | Technologie |
|---|---|
| **Frontend Web** | React 18, Vite 5, TypeScript, Workbox PWA |
| **Frontend Mobile** | Kotlin 1.9, Jetpack Compose, Material 3 |
| **Crypto** | BouncyCastle (X25519), WireGuard userspace |
| **Backend** | Supabase (Postgres + Auth + PostgREST) |
| **Infrastructure** | Cloudflare Pages + DNS + Web Analytics |
| **Observabilité** | Sentry (crash + perf) + Cloudflare Analytics |
| **CI/CD** | GitHub Actions (3 workflows) |

---

## 🚀 Démarrage rapide

### Prérequis

- **Node.js 20.11+**
- **pnpm 9+** : `npm install -g pnpm`
- **Java 17+** et **Android SDK 34** (pour Android)

### Installation

```bash
git clone https://github.com/voile/voile.git
cd voile
pnpm install
```

### Lancer la PWA en dev

```bash
pnpm dev
# → http://localhost:5173
```

### Build Android

```bash
cd packages/android
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```

### Tests

```bash
# Tests unitaires (Vitest + JUnit)
pnpm test

# Tests instrumentés Android (besoin d'un device)
cd packages/android && ./gradlew connectedDebugAndroidTest
```

---

## 📦 Déploiement

### PWA (Cloudflare Pages)

```bash
# Configure les secrets GitHub :
#   CLOUDFLARE_API_TOKEN, CLOUDFLARE_ACCOUNT_ID,
#   VITE_SUPABASE_URL, VITE_SUPABASE_ANON_KEY,
#   VITE_SENTRY_DSN, VITE_CF_ANALYTICS_TOKEN

git push origin main
# → Build + Deploy automatique sur https://voile.ricecloud.com
```

### Android (Play Store)

```bash
cd packages/android
./gradlew bundleRelease
# → app/build/outputs/bundle/release/app-release.aab
# Upload sur Google Play Console
```

---

## 🎨 Design

**Palette** (extrait depuis `@voile/core/tokens`) :

| Token | Valeur | Usage |
|---|---|---|
| `bg` | `#0A0F1C` | Background principal |
| `secured` | `#26D9C4` | État connecté (signature turquoise) |
| `connecting` | `#F5A623` | Négociation |
| `danger` | `#F0554A` | Erreurs |

**Polices** : Space Grotesk (titres), Inter (corps), JetBrains Mono (données techniques).

---

## 🧪 Tests

| Type | Framework | Couverture |
|---|---|---|
| Algo pur (Trust Score, format) | Vitest + JUnit | 100% |
| Composants React | Vitest | ~80% |
| UI Compose | createComposeRule | ~70% |
| DataStore | runTest | ✅ |
| Crypto X25519 | BouncyCastle tests | ✅ |

```bash
pnpm test                          # Tous les packages
pnpm --filter @voile/core test     # Core uniquement
cd packages/android && ./gradlew test  # Android JVM
```

---

## 🔐 Sécurité & vie privée

- **Aucune télémétrie par défaut** : Sentry envoie les erreurs, Cloudflare Analytics mesure le trafic (sans cookies).
- **Logs minimaux** : seuls les compteurs de trafic agrégés sont stockés.
- **Rétention** : sessions > 90 jours purgées automatiquement.
- **RGPD** : export/suppression de compte via `/account`.

⚠️ **Voile n'est pas un outil d'anonymat complet**. Pour un anonymat total, combine avec Tor.

### Architecture cryptographique

```
┌──────────────────────────────────────────────────────────┐
│ Client (Android)                                         │
│   • X25519 keypair (BouncyCastle, audité)               │
│   • Public key → Cloudflare /reg                        │
│   • Private key reste locale (jamais transmise)          │
└──────────────────────────────────────────────────────────┘
                        │
                        │ HTTPS (TLS 1.3)
                        ▼
┌──────────────────────────────────────────────────────────┐
│ Cloudflare Edge                                          │
│   • WireGuard handshake                                  │
│   • AES-256-GCM + ChaCha20-Poly1305                      │
│   • Perfect forward secrecy (handshake toutes les 2 min)  │
└──────────────────────────────────────────────────────────┘
```

---

## 📜 Licence

MIT © [Voile Contributors](./AUTHORS)

---

## 🙏 Remerciements

- [Cloudflare WARP](https://1.1.1.1/) pour l'infrastructure réseau
- [BouncyCastle](https://www.bouncycastle.org/) pour la crypto audité
- [WireGuard](https://www.wireguard.com/) pour le protocole VPN
- [Supabase](https://supabase.com/) pour le backend
- [Cloudflare Pages](https://pages.cloudflare.com/) pour l'hosting edge

---

## 📞 Support

- 📧 Email : support@voile.app
- 🐛 Issues : [github.com/voile/voile/issues](https://github.com/voile/voile/issues)
- 💬 Discord : [discord.gg/voile](https://discord.gg/voile)
- 📊 Status : [status.voile.ricecloud.com](https://status.voile.ricecloud.com)
