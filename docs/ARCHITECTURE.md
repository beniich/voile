# Architecture technique

## Vue d'ensemble

```
┌────────────────────────────────────────────────────────────┐
│                          Clients                           │
│                                                            │
│   ┌──────────────┐                  ┌──────────────────┐  │
│   │  PWA (Web)   │                  │  Android natif   │  │
│   │              │                  │                  │  │
│   │ • React 18   │                  │ • Kotlin 1.9     │  │
│   │ • Vite 5     │                  │ • Compose BOM    │  │
│   │ • Workbox SW │                  │ • VpnService     │  │
│   │ • Lucide     │                  │ • WireGuard      │  │
│   └──────┬───────┘                  └────────┬─────────┘  │
│          │                                   │            │
└──────────┼───────────────────────────────────┼────────────┘
           │                                   │
           └─────────────┬─────────────────────┘
                         │
                         ▼
         ┌───────────────────────────────────┐
         │         Supabase (Postgres)       │
         │                                   │
         │ • Auth (magic link + OAuth)      │
         │ • RLS sur profiles/sessions      │
         │ • RPC : log_trust_score          │
         │ • Realtime : sessions multi-dev  │
         └─────────────┬─────────────────────┘
                       │
                       ▼
         ┌───────────────────────────────────┐
         │   Cloudflare (WARP infrastructure)│
         │                                   │
         │ • Edge réseau mondial             │
         │ • WireGuard handshake             │
         │ • DNS over HTTPS (1.1.1.1)        │
         └───────────────────────────────────┘
```

## Flux de connexion (Android)

```kotlin
1. UI → VoileViewModel.toggleConnection()
2. VoileViewModel → VpnService.prepare() (vérif permission)
3. Si OK → VoileTunnelService.start() (Foreground Service)
4. Service → WarpConfigRepository.fetchConfig()
5. Repo → Cloudflare /reg (POST) → récupère config WG
6. Service → Builder.establish() (crée interface TUN)
7. Service → GoBackend.setState(UP, config) (WireGuard userspace)
8. Tunnel chiffré actif ✅
9. WorkManager → TrustScoreWorker toutes les 15 min
10. Telemetry loop → met à jour StateFlow chaque seconde
11. UI Compose → collectAsState() → re-render réactif
```

## Flux de connexion (Web)

```
1. UI → useWarp().connect()
2. Hook → fetchWarpTrace(/cdn-cgi/trace)
3. Si "warp=on" → setState(connected)
4. UI → Affiche avant/après IP, Trust Score, télémétrie
5. ⚠️ Limitation navigateur : pas de vrai tunnel système
   → utilisation d'un proxy SOCKS5 local (TODO via extension)
```

## Patterns clés

### 1. Source unique (Supabase)

Toutes les tables sont définies dans `supabase/migrations/`.
- TS : `supabase gen types typescript` → `packages/core/src/supabase/types.ts`
- Kotlin : codegen custom depuis TS → `packages/android/.../supabase/SupabaseTypes.kt`

### 2. Design tokens partagés

`packages/core/src/tokens/index.ts` (objet) → consommé par :
- Web : `import { TOKENS } from "@voile/core/tokens"`
- Android : généré en Kotlin via `pnpm gen:kotlin` → `VoileColors.kt`

### 3. Algorithmes purs

Trust Score, formatBytes, formatDuration sont **purs** (pas d'I/O, pas de dépendance plateforme).
- Testés sur les deux plateformes
- Mêmes seuils, mêmes règles

### 4. VpnService + WireGuard

Sur Android, le tunnel passe par :
1. **VpnService** : API système pour capturer le trafic
2. **Builder** : configuration TUN (IP, DNS, MTU, routes)
3. **wireguard-android:tunnel** : userspace WG (sans root)
4. **GoBackend** : implémentation Go de WireGuard

Cette stack est **la plus rapide et la plus sûre** pour un VPN Android (audité, MIT-licensed).

## Décisions architecturales (ADRs)

Voir `docs/adr/` :
- `001-monorepo-pnpm.md`
- `002-supabase-as-backend.md`
- `003-cloudflare-warp-instead-of-wireguard-nativo.md`
- `004-jetpack-compose.md`
