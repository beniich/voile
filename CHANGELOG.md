# Changelog

## [Unreleased]

### Added
- Bootstrap script : `node bootstrap.js` crée toute la structure
- Auth Supabase Kotlin complète (magic link + Google + Apple)
- ViewModel principal câblant tunnel + auth + settings
- Tests instrumentés Android (Compose + DataStore)
- Génération d'icônes PWA depuis SVG
- Documentation complète (README + USER_GUIDE + ARCHITECTURE)

### Changed
- Migration des composants web vers `@voile/core`
- Refactor de `usePersistedState` avec versionning (`voile:v1:`)

### Security
- Trust Score Worker planifié via WorkManager (pas d'I/O sur main thread)
- Token Supabase jamais loggé

## [0.1.0] - 2025-XX-XX

### Added
- UI initiale (Home, Servers, Settings)
- Sonar rings + breathe animations
- Auth Supabase basique (PWA)
- Tunnel WireGuard natif (Android)
- Trust Score algorithm v1
